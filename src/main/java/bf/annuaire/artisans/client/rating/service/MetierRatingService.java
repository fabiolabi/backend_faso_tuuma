package bf.annuaire.artisans.client.rating.service;

import bf.annuaire.artisans.ai.event.MetierAggregateChangedEvent;
import bf.annuaire.artisans.ai.event.MetierReviewSubmittedEvent;
import bf.annuaire.artisans.auth.security.AuthPrincipal;
import bf.annuaire.artisans.client.rating.dto.MetierRatingDto;
import bf.annuaire.artisans.client.rating.dto.RatingRequest;
import bf.annuaire.artisans.client.rating.entity.MetierRating;
import bf.annuaire.artisans.client.rating.entity.RatingStatus;
import bf.annuaire.artisans.client.rating.mapper.MetierRatingMapper;
import bf.annuaire.artisans.client.rating.repository.MetierRatingRepository;
import bf.annuaire.artisans.common.exception.BadRequestException;
import bf.annuaire.artisans.common.exception.ResourceNotFoundException;
import bf.annuaire.artisans.metier.entity.Metier;
import bf.annuaire.artisans.metier.repository.MetierRepository;
import bf.annuaire.artisans.user.entity.RoleName;
import bf.annuaire.artisans.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.transaction.annotation.Transactional;

@org.springframework.stereotype.Service
@RequiredArgsConstructor
public class MetierRatingService {

    private final MetierRatingRepository ratingRepository;
    private final MetierRepository metierRepository;
    private final UserRepository userRepository;
    private final MetierRatingMapper ratingMapper;
    private final ApplicationEventPublisher events;

    @Transactional
    public MetierRatingDto rate(AuthPrincipal principal, Long metierId, RatingRequest request) {
        requireClient(principal);
        Metier metier = loadVisibleMetier(metierId);
        if (metier.getOwner() != null && metier.getOwner().getId().equals(principal.userId())) {
            throw new BadRequestException("Vous ne pouvez pas noter votre propre commerce.");
        }
        MetierRating rating = ratingRepository
                .findByMetierIdAndClientId(metierId, principal.userId())
                .orElseGet(() -> {
                    MetierRating fresh = new MetierRating();
                    fresh.setClient(userRepository.getReferenceById(principal.userId()));
                    fresh.setMetier(metier);
                    return fresh;
                });
        rating.setStarRating(request.rating().shortValue());
        rating.setComment(request.comment());
        rating.setStatus(RatingStatus.PENDING);
        rating.setAiProcessedAt(null);
        MetierRating saved = ratingRepository.save(rating);
        events.publishEvent(new MetierReviewSubmittedEvent(saved.getId(), metierId));
        return ratingMapper.toDto(saved);
    }

    @Transactional
    public void deleteMine(AuthPrincipal principal, Long metierId) {
        MetierRating rating = ratingRepository
                .findByMetierIdAndClientId(metierId, principal.userId())
                .orElseThrow(() -> new ResourceNotFoundException("Aucun avis à supprimer pour ce commerce."));
        ratingRepository.delete(rating);
        events.publishEvent(new MetierAggregateChangedEvent(metierId));
    }

    @Transactional(readOnly = true)
    public Page<MetierRatingDto> listForMetier(Long metierId, Pageable pageable) {
        if (!metierRepository.existsById(metierId)) {
            throw new ResourceNotFoundException("Commerce introuvable : " + metierId);
        }
        return ratingRepository
                .findWithDetailsByMetierIdAndStatus(metierId, RatingStatus.APPROVED, pageable)
                .map(ratingMapper::toDto);
    }

    @Transactional(readOnly = true)
    public Page<MetierRatingDto> listMine(AuthPrincipal principal, Pageable pageable) {
        return ratingRepository.findWithDetailsByClientId(principal.userId(), pageable).map(ratingMapper::toDto);
    }

    private Metier loadVisibleMetier(Long metierId) {
        Metier metier = metierRepository
                .findById(metierId)
                .orElseThrow(() -> new ResourceNotFoundException("Commerce introuvable : " + metierId));
        if (!metier.isPublished() || !metier.isActive()) {
            throw new ResourceNotFoundException("Commerce introuvable : " + metierId);
        }
        return metier;
    }

    private void requireClient(AuthPrincipal principal) {
        if (principal == null
                || (!principal.roles().contains(RoleName.CLIENT.name())
                        && !principal.roles().contains(RoleName.ADMIN.name()))) {
            throw new AccessDeniedException("Seul un client peut noter un commerce.");
        }
    }
}
