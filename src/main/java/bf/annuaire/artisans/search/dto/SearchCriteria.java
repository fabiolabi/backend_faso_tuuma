package bf.annuaire.artisans.search.dto;

/**
 * Filtres de la recherche transverse (tous optionnels). {@code q} matche le nom et la description de
 * l'enseigne, ses prestations et sa localité (cf. {@code SearchRepository#search}). {@code lat}/
 * {@code lng} activent le tri par distance croissante et, avec {@code radiusKm}, le filtre par rayon.
 * La notation étant portée par les services, il n'y a plus de filtre « qualité » au niveau enseigne.
 */
public record SearchCriteria(
        String q, String categorySlug, Double lat, Double lng, Double radiusKm) {}
