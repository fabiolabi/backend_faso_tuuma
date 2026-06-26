package bf.annuaire.artisans.client.rating.repository;

import bf.annuaire.artisans.client.rating.entity.RatingStatus;
import bf.annuaire.artisans.client.rating.entity.ServiceRating;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

/** Accès aux avis de prestations ({@code service_rating}). */
public interface ServiceRatingRepository extends JpaRepository<ServiceRating, Long> {

    Optional<ServiceRating> findByServiceIdAndClientId(Long serviceId, Long clientId);

    boolean existsByServiceIdAndClientId(Long serviceId, Long clientId);

    /** Avis publics d'un service (paginés) : seuls les avis approuvés sont visibles. */
    Page<ServiceRating> findByServiceIdAndStatus(Long serviceId, RatingStatus status, Pageable pageable);

    /** Avis approuvés d'un service (liste complète) pour le recalcul de la note agrégée. */
    List<ServiceRating> findByServiceIdAndStatus(Long serviceId, RatingStatus status);

    Page<ServiceRating> findByClientId(Long clientId, Pageable pageable);

    long countByClientId(Long clientId);
}
