/**
 * Feature <strong>metier</strong> : cœur de l'annuaire géolocalisé. Gère l'enseigne d'artisan
 * ({@code metier}) et son agrégat — adresse, catégories (N–N), prestations, horaires, réseaux
 * sociaux, galerie et photo de couverture — la publication et la recherche de proximité (Haversine
 * en SQL natif, filtrée sur {@code is_published = true}).
 *
 * <p>Les notes ({@code metier_rating}) et le recalcul de {@code rating_avg} relèvent de la feature
 * {@code client} (notation) ; ils sont ici en lecture seule. Remplace l'ancien squelette {@code business}.
 */
package bf.annuaire.artisans.metier;
