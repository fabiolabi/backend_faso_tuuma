package bf.annuaire.artisans.client.rating.repository;

import bf.annuaire.artisans.client.rating.entity.MetierRating;
import bf.annuaire.artisans.client.rating.entity.RatingStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MetierRatingRepository extends JpaRepository<MetierRating, Long> {

    Optional<MetierRating> findByMetierIdAndClientId(Long metierId, Long clientId);

    Page<MetierRating> findByMetierIdAndStatus(Long metierId, RatingStatus status, Pageable pageable);

    @EntityGraph(attributePaths = {"metier", "client", "client.person"})
    Page<MetierRating> findWithDetailsByMetierIdAndStatus(
            Long metierId, RatingStatus status, Pageable pageable);

    List<MetierRating> findByMetierIdAndStatus(Long metierId, RatingStatus status);

    Page<MetierRating> findByClientId(Long clientId, Pageable pageable);

    @EntityGraph(attributePaths = {"metier", "client", "client.person"})
    Page<MetierRating> findWithDetailsByClientId(Long clientId, Pageable pageable);
}
