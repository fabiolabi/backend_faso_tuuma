package bf.annuaire.artisans.client.rating.service;

import bf.annuaire.artisans.auth.security.AuthPrincipal;
import bf.annuaire.artisans.client.rating.dto.MetierRatingDto;
import bf.annuaire.artisans.client.rating.dto.RatingRequest;
import bf.annuaire.artisans.client.rating.entity.MetierRating;
import bf.annuaire.artisans.client.rating.mapper.MetierRatingMapper;
import bf.annuaire.artisans.client.rating.repository.MetierRatingRepository;
import bf.annuaire.artisans.client.rating.repository.MetierRatingRepository.RatingAggregate;
import bf.annuaire.artisans.common.exception.BadRequestException;
import bf.annuaire.artisans.common.exception.ResourceNotFoundException;
import bf.annuaire.artisans.metier.entity.Metier;
import bf.annuaire.artisans.metier.repository.MetierRepository;
import bf.annuaire.artisans.user.entity.RoleName;
import bf.annuaire.artisans.user.repository.UserRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.transaction.annotation.Transactional;

/**
 * Notation des enseignes ({@code metier_rating}) : une note par couple (enseigne, client), en
 * upsert. Chaque écriture recalcule {@code metier.rating_avg} / {@code rating_count} à partir de
 * l'ensemble des notes de l'enseigne.
 */
@org.springframework.stereotype.Service
@RequiredArgsConstructor
public class MetierRatingService {

    private final MetierRatingRepository ratingRepository;
    private final MetierRepository metierRepository;
    private final UserRepository userRepository;
    private final MetierRatingMapper ratingMapper;

    /** Dépose ou met à jour la note du client courant pour une enseigne, puis recalcule la moyenne. */
    @Transactional
    public MetierRatingDto rate(AuthPrincipal principal, Long metierId, RatingRequest request) {
        requireClient(principal);
        Metier metier = loadVisibleMetier(metierId);
        if (metier.getOwner() != null && metier.getOwner().getId().equals(principal.userId())) {
            throw new BadRequestException("Vous ne pouvez pas noter votre propre enseigne.");
        }
        MetierRating rating = ratingRepository
                .findByMetierIdAndClientId(metierId, principal.userId())
                .orElseGet(() -> {
                    MetierRating fresh = new MetierRating();
                    fresh.setClient(userRepository.getReferenceById(principal.userId()));
                    fresh.setMetier(metier);
                    fresh.setCreatedAt(Instant.now());
                    return fresh;
                });
        rating.setRating(request.rating().shortValue());
        rating.setComment(request.comment());
        MetierRatingDto dto = ratingMapper.toDto(ratingRepository.save(rating));
        recalc(metierId);
        return dto;
    }

    /** Supprime la note du client courant pour une enseigne, puis recalcule la moyenne. */
    @Transactional
    public void deleteMine(AuthPrincipal principal, Long metierId) {
        MetierRating rating = ratingRepository
                .findByMetierIdAndClientId(metierId, principal.userId())
                .orElseThrow(() -> new ResourceNotFoundException("Aucune note à supprimer pour cette enseigne."));
        ratingRepository.delete(rating);
        recalc(metierId);
    }

    /** Avis publics d'une enseigne (paginés). */
    @Transactional(readOnly = true)
    public Page<MetierRatingDto> listForMetier(Long metierId, Pageable pageable) {
        if (!metierRepository.existsById(metierId)) {
            throw new ResourceNotFoundException("Enseigne introuvable : " + metierId);
        }
        return ratingRepository.findByMetierId(metierId, pageable).map(ratingMapper::toDto);
    }

    /** Mes notes (paginées). */
    @Transactional(readOnly = true)
    public Page<MetierRatingDto> listMine(AuthPrincipal principal, Pageable pageable) {
        return ratingRepository.findByClientId(principal.userId(), pageable).map(ratingMapper::toDto);
    }

    // ----------------------------------------------------------------- Helpers internes

    /** Recalcule {@code rating_avg} (arrondi au dixième, 0.0 si aucune note) et {@code rating_count}. */
    private void recalc(Long metierId) {
        RatingAggregate aggregate = ratingRepository.aggregateForMetier(metierId);
        Metier metier = metierRepository
                .findById(metierId)
                .orElseThrow(() -> new ResourceNotFoundException("Enseigne introuvable : " + metierId));
        long count = aggregate.getCount();
        metier.setRatingCount((int) count);
        metier.setRatingAvg(
                count == 0
                        ? BigDecimal.ZERO.setScale(1)
                        : BigDecimal.valueOf(aggregate.getAvg()).setScale(1, RoundingMode.HALF_UP));
        metierRepository.save(metier);
    }

    private Metier loadVisibleMetier(Long metierId) {
        Metier metier = metierRepository
                .findById(metierId)
                .orElseThrow(() -> new ResourceNotFoundException("Enseigne introuvable : " + metierId));
        if (!metier.isPublished() || !metier.isActive()) {
            throw new ResourceNotFoundException("Enseigne introuvable : " + metierId);
        }
        return metier;
    }

    private void requireClient(AuthPrincipal principal) {
        if (principal == null
                || (!principal.roles().contains(RoleName.CLIENT.name())
                        && !principal.roles().contains(RoleName.ADMIN.name()))) {
            throw new AccessDeniedException("Seul un client peut noter une enseigne.");
        }
    }
}
