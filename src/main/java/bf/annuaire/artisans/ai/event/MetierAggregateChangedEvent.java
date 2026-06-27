package bf.annuaire.artisans.ai.event;

/** Changement d'avis sur une enseigne (suppression, modération). */
public record MetierAggregateChangedEvent(Long metierId) {}
