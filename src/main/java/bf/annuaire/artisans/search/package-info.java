/**
 * Feature <strong>search</strong> : recherche transverse de l'annuaire et autocomplétion.
 *
 * <p>Là où {@code /api/metiers} (feature {@code metier}) ne matche le texte que sur le nom de
 * l'enseigne, {@code /api/search} élargit la portée à plusieurs sources — nom et description de
 * l'enseigne, prestations ({@code service.name}/{@code description}) et localité
 * ({@code address.city/district/sector/street}) — pour qu'un citoyen trouve une enseigne par un mot de
 * la description, une prestation (« vidange ») ou un quartier (« Gounghin »).
 *
 * <p>Feature en <strong>lecture seule</strong> : elle réutilise l'agrégat {@code metier}
 * ({@code MetierSummaryDto}, {@code MetierMapper}, {@code Category}) et reprend exactement le tri
 * Haversine SQL natif et {@code GeoUtils} de {@code metier}. Elle ne possède ni entité ni table
 * propre. Endpoints publics ({@code /api/search}, {@code /api/search/suggest}).
 */
package bf.annuaire.artisans.search;
