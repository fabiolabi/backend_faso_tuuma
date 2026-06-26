package bf.annuaire.artisans.ai.event;

/**
 * Émis quand le contenu d'une enseigne susceptible d'influer sur sa représentation sémantique change
 * (création/mise à jour de l'enseigne, ajout/modification/suppression d'un service). Déclenche, en
 * asynchrone et après commit, le recalcul de l'embedding de recherche de l'enseigne.
 */
public record MetierContentChangedEvent(Long metierId) {}
