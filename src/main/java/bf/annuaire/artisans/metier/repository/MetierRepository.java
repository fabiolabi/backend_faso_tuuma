package bf.annuaire.artisans.metier.repository;

import bf.annuaire.artisans.metier.entity.Metier;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MetierRepository extends JpaRepository<Metier, Long> {

    /** Détail d'une enseigne avec adresse, couverture et catégories chargées en une requête. */
    @EntityGraph(attributePaths = {"owner", "address", "cover", "categories"})
    Optional<Metier> findWithDetailById(Long id);

    /**
     * Candidats de la recherche sémantique : enseignes publiées, actives et déjà dotées d'un embedding.
     * {@code Pageable} sert uniquement à plafonner le nombre de candidats scorés en mémoire.
     */
    @EntityGraph(attributePaths = {"address", "cover", "categories"})
    @Query("select m from Metier m where m.published = true and m.active = true and m.searchEmbedding is not null")
    List<Metier> findSemanticCandidates(Pageable pageable);

    /** Enseignes d'un propriétaire (publiées ou non), paginées. */
    @EntityGraph(attributePaths = {"address", "cover", "categories"})
    Page<Metier> findByOwnerId(Long ownerId, Pageable pageable);

    /** Commerces visibles en recherche (publiés et actifs). */
    @EntityGraph(attributePaths = {"owner"})
    List<Metier> findByPublishedTrueAndActiveTrue();

    /** Enseignes pour affichage liste (adresse, couverture, catégories) — une requête batch. */
    @EntityGraph(attributePaths = {"address", "cover", "categories"})
    List<Metier> findSummariesByIdIn(Collection<Long> ids);

    /**
     * Recherche de proximité : renvoie uniquement les IDs (évite de charger {@code search_embedding}
     * et les associations lazy ligne par ligne).
     */
    @Query(
            value =
                    """
                    SELECT m.id FROM metier m
                    WHERE m.is_published = TRUE AND m.is_active = TRUE
                      AND (:q IS NULL OR LOWER(m.name) LIKE LOWER(CONCAT('%', :q, '%')))
                      AND (:categorySlug IS NULL OR EXISTS (
                            SELECT 1 FROM metier_category mc
                            JOIN category c ON c.id = mc.category_id
                            LEFT JOIN category p ON p.id = c.parent_id
                            WHERE mc.metier_id = m.id
                              AND (c.slug = :categorySlug OR p.slug = :categorySlug)))
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
                      AND (:q IS NULL OR LOWER(m.name) LIKE LOWER(CONCAT('%', :q, '%')))
                      AND (:categorySlug IS NULL OR EXISTS (
                            SELECT 1 FROM metier_category mc
                            JOIN category c ON c.id = mc.category_id
                            LEFT JOIN category p ON p.id = c.parent_id
                            WHERE mc.metier_id = m.id
                              AND (c.slug = :categorySlug OR p.slug = :categorySlug)))
                      AND (:lat IS NULL OR :lng IS NULL OR :radiusKm IS NULL OR (
                            m.gps_lat IS NOT NULL AND m.gps_lng IS NOT NULL
                            AND 6371 * acos(LEAST(1, cos(radians(:lat)) * cos(radians(m.gps_lat))
                                * cos(radians(m.gps_lng) - radians(:lng))
                                + sin(radians(:lat)) * sin(radians(m.gps_lat)))) <= :radiusKm))
                    """,
            nativeQuery = true)
    Page<Long> searchIds(
            @Param("q") String q,
            @Param("categorySlug") String categorySlug,
            @Param("lat") Double lat,
            @Param("lng") Double lng,
            @Param("radiusKm") Double radiusKm,
            Pageable pageable);
}
