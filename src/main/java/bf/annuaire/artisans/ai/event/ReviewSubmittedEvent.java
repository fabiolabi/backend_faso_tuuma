package bf.annuaire.artisans.ai.event;

/**
 * Émis après le dépôt (ou la mise à jour) d'un avis sur un service. Déclenche, en asynchrone et après
 * commit, l'analyse IA de l'avis ({@code ratingId}) puis le recalcul de la note agrégée et de la
 * synthèse du service ({@code serviceId}).
 */
public record ReviewSubmittedEvent(Long ratingId, Long serviceId) {}
