package bf.annuaire.artisans.notification.entity;

/** Catégorie d'une notification, pour le routage et l'affichage côté mobile. */
public enum NotificationType {
    /** Un client a déposé un avis sur une prestation de l'artisan. */
    NEW_REVIEW,
    /** Le statut d'une demande de prestation du client a changé. */
    ORDER_STATUS,
    /** Nouveau message reçu dans une conversation. */
    NEW_MESSAGE
}
