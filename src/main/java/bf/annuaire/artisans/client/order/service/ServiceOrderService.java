package bf.annuaire.artisans.client.order.service;

import bf.annuaire.artisans.auth.security.AuthPrincipal;
import bf.annuaire.artisans.client.order.dto.CreateServiceOrderRequest;
import bf.annuaire.artisans.client.order.dto.ServiceOrderDto;
import bf.annuaire.artisans.client.order.entity.ServiceOrder;
import bf.annuaire.artisans.client.order.entity.ServiceOrderStatus;
import bf.annuaire.artisans.client.order.mapper.ServiceOrderMapper;
import bf.annuaire.artisans.client.order.repository.ServiceOrderRepository;
import bf.annuaire.artisans.common.exception.BadRequestException;
import bf.annuaire.artisans.common.exception.ResourceNotFoundException;
import bf.annuaire.artisans.metier.entity.Metier;
import bf.annuaire.artisans.metier.entity.Service;
import bf.annuaire.artisans.metier.repository.MetierRepository;
import bf.annuaire.artisans.metier.repository.ServiceRepository;
import bf.annuaire.artisans.user.entity.RoleName;
import bf.annuaire.artisans.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.transaction.annotation.Transactional;

/**
 * Demandes de prestation ({@code service_order}). Côté client : création, suivi et annulation de ses
 * demandes. Côté artisan : consultation de l'inbox et transitions de statut sur les demandes
 * adressées à ses enseignes.
 */
@org.springframework.stereotype.Service
@RequiredArgsConstructor
public class ServiceOrderService {

    private final ServiceOrderRepository orderRepository;
    private final MetierRepository metierRepository;
    private final ServiceRepository serviceRepository;
    private final UserRepository userRepository;
    private final ServiceOrderMapper orderMapper;

    // ----------------------------------------------------------------- Côté client

    @Transactional
    public ServiceOrderDto create(AuthPrincipal principal, CreateServiceOrderRequest request) {
        requireClient(principal);
        Metier metier = loadVisibleMetier(request.metierId());
        ServiceOrder order = new ServiceOrder();
        order.setClient(userRepository.getReferenceById(principal.userId()));
        order.setMetier(metier);
        if (request.serviceId() != null) {
            order.setService(loadServiceOf(metier, request.serviceId()));
        }
        order.setMessage(request.message());
        order.setRequestedDate(request.requestedDate());
        order.setStatus(ServiceOrderStatus.PENDING);
        return orderMapper.toDto(orderRepository.save(order));
    }

    @Transactional(readOnly = true)
    public Page<ServiceOrderDto> listMine(AuthPrincipal principal, ServiceOrderStatus status, Pageable pageable) {
        Page<ServiceOrder> page = status == null
                ? orderRepository.findByClientId(principal.userId(), pageable)
                : orderRepository.findByClientIdAndStatus(principal.userId(), status, pageable);
        return page.map(orderMapper::toDto);
    }

    @Transactional(readOnly = true)
    public ServiceOrderDto getMine(AuthPrincipal principal, Long id) {
        return orderMapper.toDto(loadMine(principal, id));
    }

    @Transactional
    public ServiceOrderDto cancel(AuthPrincipal principal, Long id) {
        ServiceOrder order = loadMine(principal, id);
        if (order.getStatus() != ServiceOrderStatus.PENDING && order.getStatus() != ServiceOrderStatus.ACCEPTED) {
            throw new BadRequestException("Demande non annulable (statut : " + order.getStatus() + ").");
        }
        order.setStatus(ServiceOrderStatus.CANCELLED);
        return orderMapper.toDto(orderRepository.save(order));
    }

    // ----------------------------------------------------------------- Côté artisan

    @Transactional(readOnly = true)
    public Page<ServiceOrderDto> listForArtisan(AuthPrincipal principal, ServiceOrderStatus status, Pageable pageable) {
        Page<ServiceOrder> page = status == null
                ? orderRepository.findByMetier_Owner_Id(principal.userId(), pageable)
                : orderRepository.findByMetier_Owner_IdAndStatus(principal.userId(), status, pageable);
        return page.map(orderMapper::toDto);
    }

    @Transactional
    public ServiceOrderDto transition(AuthPrincipal principal, Long id, ServiceOrderStatus target) {
        ServiceOrder order = orderRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Demande introuvable : " + id));
        if (!isOwnerOrAdmin(order.getMetier(), principal)) {
            throw new AccessDeniedException("Action réservée au propriétaire de l'enseigne.");
        }
        if (!isTransitionAllowed(order.getStatus(), target)) {
            throw new BadRequestException("Transition de statut invalide : " + order.getStatus() + " → " + target + ".");
        }
        order.setStatus(target);
        return orderMapper.toDto(orderRepository.save(order));
    }

    // ----------------------------------------------------------------- Helpers internes

    private ServiceOrder loadMine(AuthPrincipal principal, Long id) {
        return orderRepository
                .findByIdAndClientId(id, principal.userId())
                .orElseThrow(() -> new ResourceNotFoundException("Demande introuvable : " + id));
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

    private Service loadServiceOf(Metier metier, Long serviceId) {
        Service service = serviceRepository
                .findById(serviceId)
                .orElseThrow(() -> new ResourceNotFoundException("Prestation introuvable : " + serviceId));
        if (!service.getMetier().getId().equals(metier.getId())) {
            throw new BadRequestException("La prestation n'appartient pas à cette enseigne.");
        }
        return service;
    }

    /**
     * Transitions autorisées : {@code PENDING → ACCEPTED | REJECTED}, {@code ACCEPTED → COMPLETED}.
     * Tout le reste est refusé (les statuts terminaux ne se rouvrent pas).
     */
    private boolean isTransitionAllowed(ServiceOrderStatus current, ServiceOrderStatus target) {
        return switch (target) {
            case ACCEPTED, REJECTED -> current == ServiceOrderStatus.PENDING;
            case COMPLETED -> current == ServiceOrderStatus.ACCEPTED;
            default -> false;
        };
    }

    private boolean isOwnerOrAdmin(Metier metier, AuthPrincipal principal) {
        if (principal == null) {
            return false;
        }
        if (principal.roles().contains(RoleName.ADMIN.name())) {
            return true;
        }
        return metier.getOwner() != null && metier.getOwner().getId().equals(principal.userId());
    }

    private void requireClient(AuthPrincipal principal) {
        if (principal == null
                || (!principal.roles().contains(RoleName.CLIENT.name())
                        && !principal.roles().contains(RoleName.ADMIN.name()))) {
            throw new AccessDeniedException("Seul un client peut déposer une demande de prestation.");
        }
    }
}
