package bf.annuaire.artisans.ai.event;

/** Nouvel avis déposé sur une enseigne. */
public record MetierReviewSubmittedEvent(Long ratingId, Long metierId) {}
