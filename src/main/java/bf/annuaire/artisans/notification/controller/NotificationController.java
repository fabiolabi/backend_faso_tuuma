package bf.annuaire.artisans.notification.controller;

import bf.annuaire.artisans.auth.security.AuthPrincipal;
import bf.annuaire.artisans.notification.dto.NotificationDto;
import bf.annuaire.artisans.notification.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Centre de notifications de l'utilisateur courant ({@code /api/notifications}). Authentification
 * requise pour toutes les opérations. L'envoi push est automatique (déclenché par les évènements
 * métier) ; ces endpoints servent à relire, compter les non-lues et marquer comme lues.
 */
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Tag(name = "Notifications", description = "Centre de notifications de l'utilisateur")
public class NotificationController {

    private final NotificationService notificationService;

    @Operation(summary = "Mes notifications (paginées, plus récentes d'abord)")
    @GetMapping
    public Page<NotificationDto> mine(
            @AuthenticationPrincipal AuthPrincipal principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return notificationService.listMine(principal, PageRequest.of(page, size));
    }

    @Operation(summary = "Nombre de notifications non lues")
    @GetMapping("/unread-count")
    public Map<String, Long> unreadCount(@AuthenticationPrincipal AuthPrincipal principal) {
        return Map.of("count", notificationService.unreadCount(principal));
    }

    @Operation(summary = "Marquer une notification comme lue")
    @PatchMapping("/{id}/read")
    public NotificationDto markRead(@PathVariable Long id, @AuthenticationPrincipal AuthPrincipal principal) {
        return notificationService.markRead(principal, id);
    }

    @Operation(summary = "Marquer toutes mes notifications comme lues")
    @PatchMapping("/read-all")
    public void markAllRead(@AuthenticationPrincipal AuthPrincipal principal) {
        notificationService.markAllRead(principal);
    }
}
