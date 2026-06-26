package bf.annuaire.artisans.client.order.controller;

import bf.annuaire.artisans.auth.security.AuthPrincipal;
import bf.annuaire.artisans.client.order.dto.CreateServiceOrderRequest;
import bf.annuaire.artisans.client.order.dto.ServiceOrderDto;
import bf.annuaire.artisans.client.order.entity.ServiceOrderStatus;
import bf.annuaire.artisans.client.order.service.ServiceOrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Demandes de prestation côté client ({@code /api/client/orders}) — dépôt, suivi et annulation de
 * ses propres demandes. Réservé au rôle {@code CLIENT}.
 */
@RestController
@RequestMapping("/api/client/orders")
@RequiredArgsConstructor
@Tag(name = "Client — Demandes", description = "Demandes de prestation déposées par le client")
public class ClientOrderController {

    private final ServiceOrderService orderService;

    @Operation(summary = "Dépôt d'une demande de prestation (CLIENT)")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ServiceOrderDto create(
            @Valid @RequestBody CreateServiceOrderRequest request,
            @AuthenticationPrincipal AuthPrincipal principal) {
        return orderService.create(principal, request);
    }

    @Operation(summary = "Mes demandes (filtre statut optionnel)")
    @GetMapping
    public Page<ServiceOrderDto> mine(
            @RequestParam(required = false) ServiceOrderStatus status,
            @AuthenticationPrincipal AuthPrincipal principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return orderService.listMine(principal, status, PageRequest.of(page, size));
    }

    @Operation(summary = "Détail d'une de mes demandes")
    @GetMapping("/{id}")
    public ServiceOrderDto get(@PathVariable Long id, @AuthenticationPrincipal AuthPrincipal principal) {
        return orderService.getMine(principal, id);
    }

    @Operation(summary = "Annulation d'une de mes demandes (PENDING ou ACCEPTED)")
    @PostMapping("/{id}/cancel")
    public ServiceOrderDto cancel(@PathVariable Long id, @AuthenticationPrincipal AuthPrincipal principal) {
        return orderService.cancel(principal, id);
    }
}
