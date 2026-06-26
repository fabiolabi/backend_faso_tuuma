package bf.annuaire.artisans.search.repository;

import bf.annuaire.artisans.metier.entity.Metier;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Requêtes de la recherche transverse, sur l'entité {@link Metier} de la feature {@code metier}
 * (réutilisée en lecture seule). La requête principale reprend le tri Haversine SQL natif de
 * {@code MetierRepository.search} et n'élargit que la clause texte (nom + description + prestations +
 * localité). Portable H2/PostgreSQL.
 */
public interface SearchRepository extends JpaRepository<Metier, Long> {

    /**
     * Recherche transverse paginée : enseignes publiées et actives uniquement. Tous les filtres sont
     * optionnels via le motif {@code (:p IS NULL OR …)}.
     *
     * <p>{@code q} matche {@code metier.name}/{@code description}, les prestations actives
     * ({@code service.name}/{@code description}) et la localité ({@code address.*}). Quand
     * {@code lat}/{@code lng} sont fournis : tri par distance Haversine croissante (et filtre par
     * {@code radiusKm} si présent) ; sinon tri par note décroissante. Le {@code CASE} renvoie {@code 0}
     * (et non {@code NULL}) en l'absence de position pour rester typé sous H2.
     */
    @Query(
            value =
                    """
                    SELECT m.* FROM metier m
                    WHERE m.is_published = TRUE AND m.is_active = TRUE
                      AND (:q IS NULL OR (
                            LOWER(m.name) LIKE LOWER(CONCAT('%', :q, '%'))
                         OR LOWER(m.description) LIKE LOWER(CONCAT('%', :q, '%'))
                         OR EXISTS (SELECT 1 FROM address a WHERE a.id = m.address_id AND (
                               LOWER(a.city) LIKE LOWER(CONCAT('%', :q, '%'))
                            OR LOWER(a.district) LIKE LOWER(CONCAT('%', :q, '%'))
                            OR LOWER(a.sector) LIKE LOWER(CONCAT('%', :q, '%'))
                            OR LOWER(a.street) LIKE LOWER(CONCAT('%', :q, '%'))))
                         OR EXISTS (SELECT 1 FROM service s WHERE s.metier_id = m.id AND s.is_active = TRUE AND (
                               LOWER(s.name) LIKE LOWER(CONCAT('%', :q, '%'))
                            OR LOWER(s.description) LIKE LOWER(CONCAT('%', :q, '%'))))))
                      AND (:categorySlug IS NULL OR EXISTS (
                            SELECT 1 FROM metier_category mc
                            JOIN category c ON c.id = mc.category_id
                            WHERE mc.metier_id = m.id AND c.slug = :categorySlug))
                      AND (:lat IS NULL OR :lng IS NULL OR :radiusKm IS NULL OR (
                            m.gps_lat IS NOT NULL AND m.gps_lng IS NOT NULL
                            AND 6371 * acos(LEAST(1, cos(radians(:lat)) * cos(radians(m.gps_lat))
                                * cos(radians(m.gps_lng) - radians(:lng))
                                + sin(radians(:lat)) * sin(radians(m.gps_lat)))) <= :radiusKm))
                    ORDER BY
                      CASE WHEN :lat IS NULL OR :lng IS NULL THEN 0 ELSE
                        6371 * acos(LEAST(1, cos(radians(:lat)) * cos(radians(m.gps_lat))
                          * cos(radians(m.gps_lng) - radians(:lng))
                          + sin(radians(:lat)) * sin(radians(m.gps_lat))))
                      END ASC,
                      m.name ASC
                    """,
            countQuery =
                    """
                    SELECT count(*) FROM metier m
                    WHERE m.is_published = TRUE AND m.is_active = TRUE
                      AND (:q IS NULL OR (
                            LOWER(m.name) LIKE LOWER(CONCAT('%', :q, '%'))
                         OR LOWER(m.description) LIKE LOWER(CONCAT('%', :q, '%'))
                         OR EXISTS (SELECT 1 FROM address a WHERE a.id = m.address_id AND (
                               LOWER(a.city) LIKE LOWER(CONCAT('%', :q, '%'))
                            OR LOWER(a.district) LIKE LOWER(CONCAT('%', :q, '%'))
                            OR LOWER(a.sector) LIKE LOWER(CONCAT('%', :q, '%'))
                            OR LOWER(a.street) LIKE LOWER(CONCAT('%', :q, '%'))))
                         OR EXISTS (SELECT 1 FROM service s WHERE s.metier_id = m.id AND s.is_active = TRUE AND (
                               LOWER(s.name) LIKE LOWER(CONCAT('%', :q, '%'))
                            OR LOWER(s.description) LIKE LOWER(CONCAT('%', :q, '%'))))))
                      AND (:categorySlug IS NULL OR EXISTS (
                            SELECT 1 FROM metier_category mc
                            JOIN category c ON c.id = mc.category_id
                            WHERE mc.metier_id = m.id AND c.slug = :categorySlug))
                      AND (:lat IS NULL OR :lng IS NULL OR :radiusKm IS NULL OR (
                            m.gps_lat IS NOT NULL AND m.gps_lng IS NOT NULL
                            AND 6371 * acos(LEAST(1, cos(radians(:lat)) * cos(radians(m.gps_lat))
                                * cos(radians(m.gps_lng) - radians(:lng))
                                + sin(radians(:lat)) * sin(radians(m.gps_lat)))) <= :radiusKm))
                    """,
            nativeQuery = true)
    Page<Metier> search(
            @Param("q") String q,
            @Param("categorySlug") String categorySlug,
            @Param("lat") Double lat,
            @Param("lng") Double lng,
            @Param("radiusKm") Double radiusKm,
            Pageable pageable);

    /** Noms distincts d'enseignes publiées et actives correspondant à {@code q} (autocomplétion). */
    @Query(
            value =
                    """
                    SELECT DISTINCT m.name FROM metier m
                    WHERE m.is_published = TRUE AND m.is_active = TRUE
                      AND LOWER(m.name) LIKE LOWER(CONCAT('%', :q, '%'))
                    ORDER BY m.name ASC
                    """,
            nativeQuery = true)
    List<String> suggestMetierNames(@Param("q") String q, Pageable pageable);

    /** Noms distincts de prestations actives d'enseignes publiées et actives correspondant à {@code q}. */
    @Query(
            value =
                    """
                    SELECT DISTINCT s.name FROM service s
                    JOIN metier m ON m.id = s.metier_id
                    WHERE s.is_active = TRUE AND m.is_published = TRUE AND m.is_active = TRUE
                      AND LOWER(s.name) LIKE LOWER(CONCAT('%', :q, '%'))
                    ORDER BY s.name ASC
                    """,
            nativeQuery = true)
    List<String> suggestServiceNames(@Param("q") String q, Pageable pageable);
}
