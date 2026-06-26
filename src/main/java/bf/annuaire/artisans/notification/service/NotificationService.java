package bf.annuaire.artisans.notification.service;

import bf.annuaire.artisans.auth.security.AuthPrincipal;
import bf.annuaire.artisans.client.PersonNames;
import bf.annuaire.artisans.client.rating.repository.ServiceRatingRepository;
import bf.annuaire.artisans.common.exception.ResourceNotFoundException;
import bf.annuaire.artisans.device.service.DeviceTokenService;
import bf.annuaire.artisans.metier.entity.Service;
import bf.annuaire.artisans.metier.repository.ServiceRepository;
import bf.annuaire.artisans.notification.dto.NotificationDto;
import bf.annuaire.artisans.notification.entity.Notification;
import bf.annuaire.artisans.notification.entity.NotificationType;
import bf.annuaire.artisans.notification.mapper.NotificationMapper;
import bf.annuaire.artisans.notification.repository.NotificationRepository;
import bf.annuaire.artisans.notification.sender.PushSender;
import bf.annuaire.artisans.user.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

/**
 * Cœur de la feature {@code notification} : persiste chaque notification (historique / centre de
 * notifications mobile) puis tente l'envoi push FCM aux appareils du destinataire, en purgeant les
 * tokens rejetés. Expose aussi la lecture (liste paginée, compteur non-lus, marquage lu) pour le
 * propriétaire courant. Les méthodes d'émission sont appelées par {@code NotificationEventListener}
 * (asynchrone, après commit), donc ouvrent leur propre transaction.
 */
@Slf4j
@org.springframework.stereotype.Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final DeviceTokenService deviceTokenService;
    private final PushSender pushSender;
    private final NotificationMapper notificationMapper;
    private final ServiceRepository serviceRepository;
    private final ServiceRatingRepository serviceRatingRepository;
    private final ObjectMapper objectMapper;

    // ----------------------------------------------------------------- Émission

    /** Crée + envoie une notification générique à un utilisateur. */
    @Transactional
    public void notify(Long recipientUserId, NotificationType type, String title, String body, Map<String, String> data) {
        persistAndPush(recipientUserId, type, title, body, data);
    }

    /** Notifie le propriétaire de l'enseigne qu'un nouvel avis a été déposé sur l'une de ses prestations. */
    @Transactional
    public void notifyNewReview(Long ratingId, Long serviceId) {
        Service service = serviceRepository.findById(serviceId).orElse(null);
        if (service == null || service.getMetier() == null || service.getMetier().getOwner() == null) {
            return;
        }
        Long ownerId = service.getMetier().getOwner().getId();
        String author = serviceRatingRepository
                .findById(ratingId)
                .map(rating -> PersonNames.fullName(rating.getClient()))
                .orElse(null);
        String who = author != null ? author : "Un client";
        String body = who + " a laissé un avis sur « " + service.getName() + " ».";
        Map<String, String> data = Map.of(
                "type", NotificationType.NEW_REVIEW.name(),
                "metierId", String.valueOf(service.getMetier().getId()),
                "serviceId", String.valueOf(serviceId));
        persistAndPush(ownerId, NotificationType.NEW_REVIEW, "Nouvel avis", body, data);
    }

    private void persistAndPush(
            Long recipientUserId, NotificationType type, String title, String body, Map<String, String> data) {
        Notification notification = new Notification();
        notification.setRecipient(userRepository.getReferenceById(recipientUserId));
        notification.setType(type);
        notification.setTitle(title);
        notification.setBody(body);
        notification.setDataJson(writeJson(data));
        notificationRepository.save(notification);
        push(recipientUserId, title, body, data);
    }

    private void push(Long recipientUserId, String title, String body, Map<String, String> data) {
        try {
            List<String> tokens = deviceTokenService.tokensOf(recipientUserId);
            if (tokens.isEmpty()) {
                return;
            }
            List<String> invalid = pushSender.send(tokens, title, body, data);
            invalid.forEach(deviceTokenService::purgeInvalid);
        } catch (Exception e) {
            log.warn("Envoi push à l'utilisateur {} échoué : {}", recipientUserId, e.getMessage());
        }
    }

    private String writeJson(Map<String, String> data) {
        if (data == null || data.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(data);
        } catch (Exception e) {
            return null;
        }
    }

    // ----------------------------------------------------------------- Lecture (mobile)

    @Transactional(readOnly = true)
    public Page<NotificationDto> listMine(AuthPrincipal principal, Pageable pageable) {
        return notificationRepository
                .findByRecipientIdOrderByCreatedAtDesc(principal.userId(), pageable)
                .map(notificationMapper::toDto);
    }

    @Transactional(readOnly = true)
    public long unreadCount(AuthPrincipal principal) {
        return notificationRepository.countByRecipientIdAndReadAtIsNull(principal.userId());
    }

    @Transactional
    public NotificationDto markRead(AuthPrincipal principal, Long id) {
        Notification notification = notificationRepository
                .findByIdAndRecipientId(id, principal.userId())
                .orElseThrow(() -> new ResourceNotFoundException("Notification introuvable : " + id));
        if (notification.getReadAt() == null) {
            notification.setReadAt(Instant.now());
            notificationRepository.save(notification);
        }
        return notificationMapper.toDto(notification);
    }

    @Transactional
    public void markAllRead(AuthPrincipal principal) {
        notificationRepository.markAllRead(principal.userId(), Instant.now());
    }
}
