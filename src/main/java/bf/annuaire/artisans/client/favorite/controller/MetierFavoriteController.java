package bf.annuaire.artisans.client.favorite.controller;

import bf.annuaire.artisans.auth.security.AuthPrincipal;
import bf.annuaire.artisans.client.favorite.service.MetierFavoriteService;
import bf.annuaire.artisans.metier.dto.MetierSummaryDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Favoris de l'utilisateur courant ({@code /api/client/favorites}). Authentification requise pour
 * toutes les opérations (un favori est rattaché à l'utilisateur connecté).
 */
@RestController
@RequestMapping("/api/client/favorites")
@RequiredArgsConstructor
@Tag(name = "Favoris", description = "Enseignes mises en favori par l'utilisateur")
public class MetierFavoriteController {

    private final MetierFavoriteService favoriteService;

    @Operation(summary = "Mes enseignes favorites (paginées)")
    @GetMapping
    public Page<MetierSummaryDto> mine(
            @AuthenticationPrincipal AuthPrincipal principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return favoriteService.listMine(principal, PageRequest.of(page, size));
    }

    @Operation(summary = "Ajouter une enseigne à mes favoris")
    @PostMapping("/{metierId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void add(@PathVariable Long metierId, @AuthenticationPrincipal AuthPrincipal principal) {
        favoriteService.add(principal, metierId);
    }

    @Operation(summary = "Retirer une enseigne de mes favoris")
    @DeleteMapping("/{metierId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void remove(@PathVariable Long metierId, @AuthenticationPrincipal AuthPrincipal principal) {
        favoriteService.remove(principal, metierId);
    }
}
