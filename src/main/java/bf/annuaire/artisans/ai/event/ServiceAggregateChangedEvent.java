package bf.annuaire.artisans.ai.event;

/**
 * Émis quand l'ensemble des avis d'un service change sans nouvel avis à analyser (ex. suppression
 * d'un avis). Déclenche, en asynchrone et après commit, le seul recalcul de la note agrégée et de la
 * synthèse du service.
 */
public record ServiceAggregateChangedEvent(Long serviceId) {}
