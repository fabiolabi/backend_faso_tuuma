package bf.annuaire.artisans.search.controller;

import bf.annuaire.artisans.metier.dto.MetierSummaryDto;
import bf.annuaire.artisans.search.dto.SearchCriteria;
import bf.annuaire.artisans.search.dto.SearchSuggestionDto;
import bf.annuaire.artisans.search.service.SearchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.math.BigDecimal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Recherche transverse de l'annuaire ({@code /api/search}) — endpoints publics. Élargit la portée
 * texte par rapport à {@code /api/metiers} (nom + description + prestations + localité) et fournit une
 * autocomplétion pour la barre de recherche mobile.
 */
@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
@Tag(name = "Recherche", description = "Recherche transverse de l'annuaire et autocomplétion")
public class SearchController {

    /** Borne haute du nombre de suggestions, pour protéger la base d'une requête abusive. */
    private static final int MAX_SUGGESTIONS = 20;

    private final SearchService searchService;

    @Operation(summary = "Recherche transverse paginée (nom, description, prestations, localité)")
    @GetMapping
    public Page<MetierSummaryDto> search(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String categorySlug,
            @RequestParam(required = false) BigDecimal minRating,
            @RequestParam(required = false) Double lat,
            @RequestParam(required = false) Double lng,
            @RequestParam(required = false) Double radiusKm,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        SearchCriteria criteria = new SearchCriteria(q, categorySlug, minRating, lat, lng, radiusKm);
        return searchService.search(criteria, PageRequest.of(page, size));
    }

    @Operation(summary = "Suggestions d'autocomplétion (catégories, enseignes, prestations)")
    @GetMapping("/suggest")
    public List<SearchSuggestionDto> suggest(
            @RequestParam String q, @RequestParam(defaultValue = "10") int limit) {
        return searchService.suggest(q, Math.min(Math.max(limit, 0), MAX_SUGGESTIONS));
    }
}
