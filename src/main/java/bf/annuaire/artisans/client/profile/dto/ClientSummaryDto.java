package bf.annuaire.artisans.client.profile.dto;

/** Résumé d'activité de l'espace client : nombre de demandes par statut, de notes et de fils. */
public record ClientSummaryDto(
        long ordersPending,
        long ordersAccepted,
        long ordersCompleted,
        long ordersRejected,
        long ordersCancelled,
        long ratingsCount,
        long conversationsCount) {}
