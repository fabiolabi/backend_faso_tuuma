package bf.annuaire.artisans.client.order.controller;

import bf.annuaire.artisans.auth.security.AuthPrincipal;
import bf.annuaire.artisans.client.order.dto.ServiceOrderDto;
import bf.annuaire.artisans.client.order.entity.ServiceOrderStatus;
import bf.annuaire.artisans.client.order.service.ServiceOrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Demandes de prestation côté artisan ({@code /api/artisan/orders}) — inbox et transitions de statut
 * sur les demandes adressées aux enseignes possédées par l'appelant.
 *
 * <p>Volontairement hors de {@code /api/metiers/**} : ce préfixe est public en GET
 * ({@code SecurityConfig.PUBLIC_GET}), il n'exposerait donc pas correctement cette inbox protégée.
 */
@RestController
@RequestMapping("/api/artisan/orders")
@RequiredArgsConstructor
@Tag(name = "Artisan — Demandes", description = "Traitement des demandes reçues par l'artisan")
public class ArtisanOrderController {

    private final ServiceOrderService orderService;

    @Operation(summary = "Demandes reçues sur mes enseignes (filtre statut optionnel)")
    @GetMapping
    public Page<ServiceOrderDto> inbox(
            @RequestParam(required = false) ServiceOrderStatus status,
            @AuthenticationPrincipal AuthPrincipal principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return orderService.listForArtisan(principal, status, PageRequest.of(page, size));
    }

    @Operation(summary = "Accepter une demande (propriétaire ou ADMIN)")
    @PostMapping("/{id}/accept")
    public ServiceOrderDto accept(@PathVariable Long id, @AuthenticationPrincipal AuthPrincipal principal) {
        return orderService.transition(principal, id, ServiceOrderStatus.ACCEPTED);
    }

    @Operation(summary = "Refuser une demande (propriétaire ou ADMIN)")
    @PostMapping("/{id}/reject")
    public ServiceOrderDto reject(@PathVariable Long id, @AuthenticationPrincipal AuthPrincipal principal) {
        return orderService.transition(principal, id, ServiceOrderStatus.REJECTED);
    }

    @Operation(summary = "Clôturer une demande acceptée (propriétaire ou ADMIN)")
    @PostMapping("/{id}/complete")
    public ServiceOrderDto complete(@PathVariable Long id, @AuthenticationPrincipal AuthPrincipal principal) {
        return orderService.transition(principal, id, ServiceOrderStatus.COMPLETED);
    }
}
