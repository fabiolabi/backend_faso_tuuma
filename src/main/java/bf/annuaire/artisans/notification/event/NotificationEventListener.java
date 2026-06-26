package bf.annuaire.artisans.notification.event;

import bf.annuaire.artisans.ai.event.ReviewSubmittedEvent;
import bf.annuaire.artisans.notification.entity.NotificationType;
import bf.annuaire.artisans.notification.service.NotificationService;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Déclenche les notifications push à partir des évènements métier. Les écouteurs se déclenchent
 * <strong>après le commit</strong> de la transaction émettrice et s'exécutent sur le pool
 * {@code notificationExecutor}, afin de ne jamais ralentir la requête HTTP. Chaque traitement
 * journalise sans relancer en cas d'échec ; {@link NotificationService} ouvre sa propre transaction.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationEventListener {

    private final NotificationService notificationService;

    /** Nouvel avis → notifie le propriétaire de l'enseigne. */
    @Async("notificationExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onReviewSubmitted(ReviewSubmittedEvent event) {
        try {
            notificationService.notifyNewReview(event.ratingId(), event.serviceId());
        } catch (Exception e) {
            log.warn("Notification d'avis {} échouée : {}", event.ratingId(), e.getMessage());
        }
    }

    /** Changement de statut d'une demande → notifie le client. */
    @Async("notificationExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onOrderStatusChanged(OrderStatusChangedEvent event) {
        try {
            String label = statusLabel(event.newStatus());
            String body = "Votre demande auprès de « " + event.metierName() + " » est désormais : " + label + ".";
            notificationService.notify(
                    event.recipientUserId(),
                    NotificationType.ORDER_STATUS,
                    "Demande " + label,
                    body,
                    Map.of("type", NotificationType.ORDER_STATUS.name(), "orderId", String.valueOf(event.orderId())));
        } catch (Exception e) {
            log.warn("Notification de statut de commande {} échouée : {}", event.orderId(), e.getMessage());
        }
    }

    /** Nouveau message → notifie l'autre participant. */
    @Async("notificationExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onMessageSent(MessageSentEvent event) {
        try {
            String sender = event.senderName() != null ? event.senderName() : "Quelqu'un";
            String body = event.preview() != null ? sender + " : " + event.preview() : sender + " vous a écrit.";
            notificationService.notify(
                    event.recipientUserId(),
                    NotificationType.NEW_MESSAGE,
                    "Nouveau message",
                    body,
                    Map.of(
                            "type", NotificationType.NEW_MESSAGE.name(),
                            "conversationId", String.valueOf(event.conversationId())));
        } catch (Exception e) {
            log.warn("Notification de message (conversation {}) échouée : {}", event.conversationId(), e.getMessage());
        }
    }

    /** Libellé français lisible d'un statut de demande ({@code ServiceOrderStatus}). */
    private String statusLabel(String status) {
        return switch (status) {
            case "ACCEPTED" -> "acceptée";
            case "REJECTED" -> "refusée";
            case "COMPLETED" -> "terminée";
            case "CANCELLED" -> "annulée";
            case "PENDING" -> "en attente";
            default -> status;
        };
    }
}
