package bf.annuaire.artisans.metier.dto;

/**
 * Filtres de la recherche de proximité (tous optionnels). {@code lat}/{@code lng} activent le tri par
 * distance croissante et, avec {@code radiusKm}, le filtre par rayon. La notation étant portée par
 * les services, il n'y a plus de filtre « qualité » au niveau enseigne.
 */
public record MetierSearchCriteria(
        String q, String categorySlug, Double lat, Double lng, Double radiusKm) {}
