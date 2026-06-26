package bf.annuaire.artisans.client.rating.service;

import bf.annuaire.artisans.ai.event.ReviewSubmittedEvent;
import bf.annuaire.artisans.ai.event.ServiceAggregateChangedEvent;
import bf.annuaire.artisans.auth.security.AuthPrincipal;
import bf.annuaire.artisans.client.rating.dto.RatingRequest;
import bf.annuaire.artisans.client.rating.dto.ServiceRatingDto;
import bf.annuaire.artisans.client.rating.entity.RatingStatus;
import bf.annuaire.artisans.client.rating.entity.ServiceRating;
import bf.annuaire.artisans.client.rating.mapper.ServiceRatingMapper;
import bf.annuaire.artisans.client.rating.repository.ServiceRatingRepository;
import bf.annuaire.artisans.common.exception.BadRequestException;
import bf.annuaire.artisans.common.exception.ResourceNotFoundException;
import bf.annuaire.artisans.metier.entity.Metier;
import bf.annuaire.artisans.metier.entity.Service;
import bf.annuaire.artisans.metier.repository.ServiceRepository;
import bf.annuaire.artisans.user.entity.RoleName;
import bf.annuaire.artisans.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.transaction.annotation.Transactional;

/**
 * Dépôt des avis sur les prestations ({@code service_rating}) : un avis par couple (service, client),
 * en upsert. L'avis est enregistré immédiatement au statut {@code PENDING} ; l'analyse IA (sentiment,
 * faux avis, modération) et le recalcul de la note du service sont délégués à la feature {@code ai}
 * via des événements traités <strong>après commit, en asynchrone</strong>.
 */
@org.springframework.stereotype.Service
@RequiredArgsConstructor
public class ServiceRatingService {

    private final ServiceRatingRepository ratingRepository;
    private final ServiceRepository serviceRepository;
    private final UserRepository userRepository;
    private final ServiceRatingMapper ratingMapper;
    private final ApplicationEventPublisher events;

    /** Dépose ou met à jour mon avis sur une prestation, puis déclenche l'analyse IA asynchrone. */
    @Transactional
    public ServiceRatingDto rate(AuthPrincipal principal, Long serviceId, RatingRequest request) {
        requireClient(principal);
        Service service = loadVisibleService(serviceId);
        Metier metier = service.getMetier();
        if (metier.getOwner() != null && metier.getOwner().getId().equals(principal.userId())) {
            throw new BadRequestException("Vous ne pouvez pas noter votre propre prestation.");
        }
        ServiceRating rating = ratingRepository
                .findByServiceIdAndClientId(serviceId, principal.userId())
                .orElseGet(() -> {
                    ServiceRating fresh = new ServiceRating();
                    fresh.setClient(userRepository.getReferenceById(principal.userId()));
                    fresh.setService(service);
                    return fresh;
                });
        rating.setStarRating(request.rating().shortValue());
        rating.setComment(request.comment());
        // Remise à zéro de l'analyse : l'avis doit repasser par l'IA (statut non public en attendant).
        rating.setStatus(RatingStatus.PENDING);
        rating.setAiProcessedAt(null);
        ServiceRating saved = ratingRepository.save(rating);
        ServiceRatingDto dto = ratingMapper.toDto(saved);
        events.publishEvent(new ReviewSubmittedEvent(saved.getId(), serviceId));
        return dto;
    }

    /** Supprime mon avis sur une prestation, puis déclenche le recalcul de la note du service. */
    @Transactional
    public void deleteMine(AuthPrincipal principal, Long serviceId) {
        ServiceRating rating = ratingRepository
                .findByServiceIdAndClientId(serviceId, principal.userId())
                .orElseThrow(() -> new ResourceNotFoundException("Aucun avis à supprimer pour cette prestation."));
        ratingRepository.delete(rating);
        events.publishEvent(new ServiceAggregateChangedEvent(serviceId));
    }

    /** Avis publics d'une prestation (paginés) : uniquement les avis approuvés. */
    @Transactional(readOnly = true)
    public Page<ServiceRatingDto> listForService(Long serviceId, Pageable pageable) {
        if (!serviceRepository.existsById(serviceId)) {
            throw new ResourceNotFoundException("Prestation introuvable : " + serviceId);
        }
        return ratingRepository
                .findByServiceIdAndStatus(serviceId, RatingStatus.APPROVED, pageable)
                .map(ratingMapper::toDto);
    }

    /** Mes avis (toutes prestations confondues, paginés) — y compris en attente/rejetés. */
    @Transactional(readOnly = true)
    public Page<ServiceRatingDto> listMine(AuthPrincipal principal, Pageable pageable) {
        return ratingRepository.findByClientId(principal.userId(), pageable).map(ratingMapper::toDto);
    }

    // ----------------------------------------------------------------- Helpers internes

    /** Charge une prestation visible (active, d'une enseigne publiée et active), sinon 404. */
    private Service loadVisibleService(Long serviceId) {
        Service service = serviceRepository
                .findById(serviceId)
                .orElseThrow(() -> new ResourceNotFoundException("Prestation introuvable : " + serviceId));
        Metier metier = service.getMetier();
        if (!service.isActive() || metier == null || !metier.isPublished() || !metier.isActive()) {
            throw new ResourceNotFoundException("Prestation introuvable : " + serviceId);
        }
        return service;
    }

    private void requireClient(AuthPrincipal principal) {
        if (principal == null
                || (!principal.roles().contains(RoleName.CLIENT.name())
                        && !principal.roles().contains(RoleName.ADMIN.name()))) {
            throw new AccessDeniedException("Seul un client peut noter une prestation.");
        }
    }
}
