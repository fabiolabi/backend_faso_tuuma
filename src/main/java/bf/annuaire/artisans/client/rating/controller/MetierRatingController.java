package bf.annuaire.artisans.client.rating.controller;

import bf.annuaire.artisans.auth.security.AuthPrincipal;
import bf.annuaire.artisans.client.rating.dto.MetierRatingDto;
import bf.annuaire.artisans.client.rating.dto.RatingRequest;
import bf.annuaire.artisans.client.rating.service.MetierRatingService;
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
 * Notation des enseignes. Les avis d'une enseigne sont publics en lecture
 * ({@code GET /api/metiers/{id}/ratings}, couvert par le préfixe public {@code /api/metiers/**}) ;
 * déposer, mettre à jour ou supprimer sa note exige le rôle {@code CLIENT}.
 */
@RestController
@RequiredArgsConstructor
@Tag(name = "Notation", description = "Notes et avis des clients sur les enseignes")
public class MetierRatingController {

    private final MetierRatingService ratingService;

    @Operation(summary = "Avis publics d'une enseigne (paginés)")
    @GetMapping("/api/metiers/{metierId}/ratings")
    public Page<MetierRatingDto> forMetier(
            @PathVariable Long metierId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ratingService.listForMetier(metierId, PageRequest.of(page, size));
    }

    @Operation(summary = "Déposer ou mettre à jour ma note sur une enseigne (CLIENT)")
    @PostMapping("/api/metiers/{metierId}/ratings")
    public MetierRatingDto rate(
            @PathVariable Long metierId,
            @Valid @RequestBody RatingRequest request,
            @AuthenticationPrincipal AuthPrincipal principal) {
        return ratingService.rate(principal, metierId, request);
    }

    @Operation(summary = "Supprimer ma note sur une enseigne (CLIENT)")
    @DeleteMapping("/api/metiers/{metierId}/ratings/mine")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteMine(@PathVariable Long metierId, @AuthenticationPrincipal AuthPrincipal principal) {
        ratingService.deleteMine(principal, metierId);
    }

    @Operation(summary = "Mes notes (toutes enseignes confondues)")
    @GetMapping("/api/client/ratings")
    public Page<MetierRatingDto> mine(
            @AuthenticationPrincipal AuthPrincipal principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ratingService.listMine(principal, PageRequest.of(page, size));
    }
}
