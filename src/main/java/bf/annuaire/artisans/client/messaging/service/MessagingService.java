package bf.annuaire.artisans.client.messaging.service;

import bf.annuaire.artisans.auth.security.AuthPrincipal;
import bf.annuaire.artisans.client.PersonNames;
import bf.annuaire.artisans.client.messaging.dto.ConversationDto;
import bf.annuaire.artisans.client.messaging.dto.MessageDto;
import bf.annuaire.artisans.client.messaging.dto.SendMessageRequest;
import bf.annuaire.artisans.client.messaging.dto.StartConversationRequest;
import bf.annuaire.artisans.client.messaging.entity.Conversation;
import bf.annuaire.artisans.client.messaging.entity.Message;
import bf.annuaire.artisans.client.messaging.mapper.MessageMapper;
import bf.annuaire.artisans.client.messaging.repository.ConversationRepository;
import bf.annuaire.artisans.client.messaging.repository.MessageRepository;
import bf.annuaire.artisans.common.exception.ResourceNotFoundException;
import bf.annuaire.artisans.metier.entity.Metier;
import bf.annuaire.artisans.metier.repository.MetierRepository;
import bf.annuaire.artisans.user.entity.RoleName;
import bf.annuaire.artisans.user.repository.UserRepository;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.transaction.annotation.Transactional;

/**
 * Messagerie client↔artisan ({@code conversation} / {@code message}), persistée et exposée en REST.
 * Au plus un fil par couple (client, enseigne). Service volontairement agnostique du transport :
 * une couche WebSocket future pourra le réutiliser pour diffuser en temps réel.
 */
@org.springframework.stereotype.Service
@RequiredArgsConstructor
public class MessagingService {

    private static final int PREVIEW_MAX = 120;

    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final MetierRepository metierRepository;
    private final UserRepository userRepository;
    private final MessageMapper messageMapper;

    /** Démarre ou récupère le fil du client courant avec une enseigne ; envoie éventuellement un 1er message. */
    @Transactional
    public ConversationDto startOrGet(AuthPrincipal principal, StartConversationRequest request) {
        requireClient(principal);
        Metier metier = loadVisibleMetier(request.metierId());
        Conversation conversation = conversationRepository
                .findByClientIdAndMetierId(principal.userId(), request.metierId())
                .orElseGet(() -> {
                    Conversation fresh = new Conversation();
                    fresh.setClient(userRepository.getReferenceById(principal.userId()));
                    fresh.setMetier(metier);
                    fresh.setCreatedAt(Instant.now());
                    return conversationRepository.save(fresh);
                });
        if (request.firstMessage() != null && !request.firstMessage().isBlank()) {
            appendMessage(conversation, principal.userId(), request.firstMessage());
        }
        return toDto(conversation, principal.userId());
    }

    @Transactional(readOnly = true)
    public Page<ConversationDto> listConversations(AuthPrincipal principal, String box, Pageable pageable) {
        Long userId = principal.userId();
        Page<Conversation> page = "artisan".equalsIgnoreCase(box)
                ? conversationRepository.findByMetier_Owner_IdOrderByLastMessageAtDesc(userId, pageable)
                : conversationRepository.findByClientIdOrderByLastMessageAtDesc(userId, pageable);
        return page.map(conversation -> toDto(conversation, userId));
    }

    @Transactional(readOnly = true)
    public ConversationDto getConversation(AuthPrincipal principal, Long conversationId) {
        Conversation conversation = loadParticipating(conversationId, principal);
        return toDto(conversation, principal.userId());
    }

    @Transactional(readOnly = true)
    public Page<MessageDto> listMessages(AuthPrincipal principal, Long conversationId, Pageable pageable) {
        loadParticipating(conversationId, principal);
        return messageRepository
                .findByConversationIdOrderBySentAtDesc(conversationId, pageable)
                .map(messageMapper::toDto);
    }

    @Transactional
    public MessageDto send(AuthPrincipal principal, Long conversationId, SendMessageRequest request) {
        Conversation conversation = loadParticipating(conversationId, principal);
        Message message = appendMessage(conversation, principal.userId(), request.body());
        return messageMapper.toDto(message);
    }

    /** Marque comme lus les messages reçus (de l'autre partie) dans le fil. */
    @Transactional
    public void markRead(AuthPrincipal principal, Long conversationId) {
        loadParticipating(conversationId, principal);
        messageRepository.markRead(conversationId, principal.userId(), Instant.now());
    }

    // ----------------------------------------------------------------- Helpers internes

    private Message appendMessage(Conversation conversation, Long senderId, String body) {
        Instant now = Instant.now();
        Message message = new Message();
        message.setConversation(conversation);
        message.setSender(userRepository.getReferenceById(senderId));
        message.setBody(body);
        message.setSentAt(now);
        Message saved = messageRepository.save(message);
        conversation.setLastMessageAt(now);
        conversationRepository.save(conversation);
        return saved;
    }

    private ConversationDto toDto(Conversation conversation, Long readerId) {
        String preview = messageRepository
                .findTop1ByConversationIdOrderBySentAtDesc(conversation.getId())
                .map(message -> truncate(message.getBody()))
                .orElse(null);
        long unread = messageRepository.countByConversationIdAndSender_IdNotAndReadAtIsNull(
                conversation.getId(), readerId);
        return new ConversationDto(
                conversation.getId(),
                conversation.getMetier().getId(),
                conversation.getMetier().getName(),
                conversation.getClient().getId(),
                PersonNames.fullName(conversation.getClient()),
                conversation.getLastMessageAt(),
                preview,
                unread,
                conversation.getCreatedAt());
    }

    private String truncate(String body) {
        if (body == null) {
            return null;
        }
        return body.length() <= PREVIEW_MAX ? body : body.substring(0, PREVIEW_MAX) + "…";
    }

    private Conversation loadParticipating(Long conversationId, AuthPrincipal principal) {
        Conversation conversation = conversationRepository
                .findById(conversationId)
                .orElseThrow(() -> new ResourceNotFoundException("Conversation introuvable : " + conversationId));
        if (!isParticipant(conversation, principal)) {
            // 404 plutôt que 403 : ne pas révéler l'existence d'un fil qui n'est pas le sien.
            throw new ResourceNotFoundException("Conversation introuvable : " + conversationId);
        }
        return conversation;
    }

    private boolean isParticipant(Conversation conversation, AuthPrincipal principal) {
        if (principal == null) {
            return false;
        }
        if (principal.roles().contains(RoleName.ADMIN.name())) {
            return true;
        }
        Long userId = principal.userId();
        boolean isClient = conversation.getClient() != null && conversation.getClient().getId().equals(userId);
        boolean isOwner = conversation.getMetier() != null
                && conversation.getMetier().getOwner() != null
                && conversation.getMetier().getOwner().getId().equals(userId);
        return isClient || isOwner;
    }

    private Metier loadVisibleMetier(Long metierId) {
        Metier metier = metierRepository
                .findById(metierId)
                .orElseThrow(() -> new ResourceNotFoundException("Enseigne introuvable : " + metierId));
        if (!metier.isPublished() || !metier.isActive()) {
            throw new ResourceNotFoundException("Enseigne introuvable : " + metierId);
        }
        return metier;
    }

    private void requireClient(AuthPrincipal principal) {
        if (principal == null
                || (!principal.roles().contains(RoleName.CLIENT.name())
                        && !principal.roles().contains(RoleName.ADMIN.name()))) {
            throw new AccessDeniedException("Seul un client peut démarrer une conversation.");
        }
    }
}
