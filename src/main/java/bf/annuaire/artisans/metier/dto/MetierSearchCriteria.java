package bf.annuaire.artisans.metier.dto;

import java.math.BigDecimal;

/**
 * Filtres de la recherche de proximité (tous optionnels). {@code lat}/{@code lng} activent le tri par
 * distance croissante et, avec {@code radiusKm}, le filtre par rayon.
 */
public record MetierSearchCriteria(
        String q,
        String categorySlug,
        BigDecimal minRating,
        Double lat,
        Double lng,
        Double radiusKm) {}
