package bf.annuaire.artisans.client.rating.repository;

import bf.annuaire.artisans.client.rating.entity.MetierRating;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/** Accès aux notes d'enseignes ({@code metier_rating}) + agrégat pour le recalcul de la moyenne. */
public interface MetierRatingRepository extends JpaRepository<MetierRating, Long> {

    Optional<MetierRating> findByMetierIdAndClientId(Long metierId, Long clientId);

    boolean existsByMetierIdAndClientId(Long metierId, Long clientId);

    Page<MetierRating> findByMetierId(Long metierId, Pageable pageable);

    Page<MetierRating> findByClientId(Long clientId, Pageable pageable);

    long countByClientId(Long clientId);

    /** Moyenne et nombre de notes d'une enseigne, pour recalculer {@code metier.rating_avg/count}. */
    @Query("select coalesce(avg(r.rating), 0) as avg, count(r) as count "
            + "from MetierRating r where r.metier.id = :metierId")
    RatingAggregate aggregateForMetier(@Param("metierId") Long metierId);

    /** Projection de l'agrégat de notation. */
    interface RatingAggregate {
        Double getAvg();

        long getCount();
    }
}
