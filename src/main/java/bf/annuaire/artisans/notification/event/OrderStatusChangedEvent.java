package bf.annuaire.artisans.notification.event;

/**
 * Émis après le changement de statut d'une demande de prestation. Déclenche, en asynchrone et après
 * commit, une notification au client ({@code recipientUserId}). Porte les libellés nécessaires pour
 * éviter toute navigation lazy hors transaction côté écouteur.
 */
public record OrderStatusChangedEvent(Long orderId, Long recipientUserId, String newStatus, String metierName) {}
