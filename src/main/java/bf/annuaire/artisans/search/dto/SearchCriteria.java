package bf.annuaire.artisans.search.dto;

import java.math.BigDecimal;

/**
 * Filtres de la recherche transverse (tous optionnels). {@code q} matche le nom et la description de
 * l'enseigne, ses prestations et sa localité (cf. {@code SearchRepository#search}). {@code lat}/
 * {@code lng} activent le tri par distance croissante et, avec {@code radiusKm}, le filtre par rayon.
 */
public record SearchCriteria(
        String q,
        String categorySlug,
        BigDecimal minRating,
        Double lat,
        Double lng,
        Double radiusKm) {}
