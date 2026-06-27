package bf.annuaire.artisans.client.rating.controller;

import bf.annuaire.artisans.auth.security.AuthPrincipal;
import bf.annuaire.artisans.client.rating.dto.RatingRequest;
import bf.annuaire.artisans.client.rating.dto.ServiceRatingDto;
import bf.annuaire.artisans.client.rating.service.ServiceRatingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Notation des prestations. Les avis approuvés d'une prestation sont publics en lecture
 * ({@code GET /api/services/{serviceId}/ratings}) ; déposer, mettre à jour ou supprimer son avis
 * exige le rôle {@code CLIENT}. La note affichée est calculée par l'IA (feature {@code ai}).
 */
@RestController
@RequiredArgsConstructor
@Tag(name = "Notation", description = "Avis des clients sur les prestations (note IA)")
public class ServiceRatingController {

    private final ServiceRatingService ratingService;

    @Operation(summary = "Avis publics (approuvés) d'une prestation, paginés")
    @GetMapping("/api/services/{serviceId}/ratings")
    public Page<ServiceRatingDto> forService(
            @PathVariable Long serviceId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ratingService.listForService(serviceId, PageRequest.of(page, size));
    }

    @Operation(summary = "Déposer ou mettre à jour mon avis sur une prestation (CLIENT)")
    @PostMapping("/api/services/{serviceId}/ratings")
    public ServiceRatingDto rate(
            @PathVariable Long serviceId,
            @Valid @RequestBody RatingRequest request,
            @AuthenticationPrincipal AuthPrincipal principal) {
        return ratingService.rate(principal, serviceId, request);
    }

    @Operation(summary = "Supprimer mon avis sur une prestation (CLIENT)")
    @DeleteMapping("/api/services/{serviceId}/ratings/mine")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteMine(@PathVariable Long serviceId, @AuthenticationPrincipal AuthPrincipal principal) {
        ratingService.deleteMine(principal, serviceId);
    }

    @Operation(summary = "Mes avis (toutes prestations confondues)")
    @GetMapping("/api/client/service-ratings")
    public Page<ServiceRatingDto> mine(
            @AuthenticationPrincipal AuthPrincipal principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ratingService.listMine(principal, PageRequest.of(page, size));
    }
}
