package bf.annuaire.artisans.client.messaging.controller;

import bf.annuaire.artisans.auth.security.AuthPrincipal;
import bf.annuaire.artisans.client.messaging.dto.ConversationDto;
import bf.annuaire.artisans.client.messaging.dto.MessageDto;
import bf.annuaire.artisans.client.messaging.dto.SendMessageRequest;
import bf.annuaire.artisans.client.messaging.dto.StartConversationRequest;
import bf.annuaire.artisans.client.messaging.service.MessagingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Messagerie client↔artisan ({@code /api/conversations}). Réservée aux participants d'un fil (le
 * client à l'origine, le propriétaire de l'enseigne, ou un {@code ADMIN}). Seul un {@code CLIENT}
 * peut démarrer un fil.
 */
@RestController
@RequestMapping("/api/conversations")
@RequiredArgsConstructor
@Tag(name = "Messagerie", description = "Conversations et messages client↔artisan")
public class ConversationController {

    private final MessagingService messagingService;

    @Operation(summary = "Démarrer ou récupérer un fil avec une enseigne (CLIENT)")
    @PostMapping
    public ConversationDto start(
            @Valid @RequestBody StartConversationRequest request,
            @AuthenticationPrincipal AuthPrincipal principal) {
        return messagingService.startOrGet(principal, request);
    }

    @Operation(summary = "Mes fils de discussion (box=client par défaut, ou box=artisan)")
    @GetMapping
    public Page<ConversationDto> list(
            @RequestParam(defaultValue = "client") String box,
            @AuthenticationPrincipal AuthPrincipal principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return messagingService.listConversations(principal, box, PageRequest.of(page, size));
    }

    @Operation(summary = "Détail d'un fil (participant)")
    @GetMapping("/{id}")
    public ConversationDto get(@PathVariable Long id, @AuthenticationPrincipal AuthPrincipal principal) {
        return messagingService.getConversation(principal, id);
    }

    @Operation(summary = "Messages d'un fil, du plus récent au plus ancien (participant)")
    @GetMapping("/{id}/messages")
    public Page<MessageDto> messages(
            @PathVariable Long id,
            @AuthenticationPrincipal AuthPrincipal principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return messagingService.listMessages(principal, id, PageRequest.of(page, size));
    }

    @Operation(summary = "Envoyer un message dans un fil (participant)")
    @PostMapping("/{id}/messages")
    @ResponseStatus(HttpStatus.CREATED)
    public MessageDto send(
            @PathVariable Long id,
            @Valid @RequestBody SendMessageRequest request,
            @AuthenticationPrincipal AuthPrincipal principal) {
        return messagingService.send(principal, id, request);
    }

    @Operation(summary = "Marquer le fil comme lu (participant)")
    @PostMapping("/{id}/read")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void read(@PathVariable Long id, @AuthenticationPrincipal AuthPrincipal principal) {
        messagingService.markRead(principal, id);
    }
}
