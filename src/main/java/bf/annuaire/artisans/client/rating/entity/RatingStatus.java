package bf.annuaire.artisans.client.rating.entity;

/**
 * État de modération d'un avis, piloté par l'analyse IA asynchrone.
 *
 * <ul>
 *   <li>{@code PENDING} : avis déposé, pas encore analysé (invisible du public, exclu de la note).
 *   <li>{@code APPROVED} : avis analysé et retenu (visible, pris en compte dans la note pondérée).
 *   <li>{@code REJECTED} : avis jugé faux/abusif ou inapproprié (masqué, exclu de la note).
 * </ul>
 */
public enum RatingStatus {
    PENDING,
    APPROVED,
    REJECTED
}
