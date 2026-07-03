package bf.annuaire.artisans.search.service;

import bf.annuaire.artisans.common.util.GeoUtils;
import bf.annuaire.artisans.metier.entity.Category;
import bf.annuaire.artisans.metier.entity.Metier;
import bf.annuaire.artisans.metier.dto.MetierSummaryDto;
import bf.annuaire.artisans.metier.mapper.MetierMapper;
import bf.annuaire.artisans.metier.service.MetierHydrationService;
import bf.annuaire.artisans.metier.repository.CategoryRepository;
import bf.annuaire.artisans.search.dto.SearchCriteria;
import bf.annuaire.artisans.search.dto.SearchSuggestionDto;
import bf.annuaire.artisans.search.repository.SearchRepository;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Recherche transverse de l'annuaire (lecture seule). Réutilise l'agrégat {@code metier} :
 * {@link MetierMapper} pour la vue résumée, {@link GeoUtils} pour la distance et {@link Metier}/
 * {@link Category} comme sources. Le tri par proximité est fait en SQL natif (cf.
 * {@link SearchRepository#searchIds}) ; le service ne fait qu'enrichir le {@code distanceKm} affiché.
 */
@Service
@RequiredArgsConstructor
public class SearchService {

    private final SearchRepository searchRepository;
    private final MetierHydrationService metierHydration;
    private final CategoryRepository categoryRepository;
    private final MetierMapper metierMapper;

    /** Recherche transverse paginée (enseignes publiées et actives uniquement). */
    @Transactional(readOnly = true)
    public Page<MetierSummaryDto> search(SearchCriteria criteria, Pageable pageable) {
        return metierHydration.mapIdPage(
                searchRepository.searchIds(
                        criteria.q(),
                        criteria.categorySlug(),
                        criteria.lat(),
                        criteria.lng(),
                        criteria.radiusKm(),
                        pageable),
                metier -> metierMapper.toSummary(metier, distanceFor(metier, criteria)));
    }

    /**
     * Suggestions d'autocomplétion (catégories, puis enseignes, puis prestations), plafonnées à
     * {@code limit} au total et dédupliquées sur le libellé. Requête vide ⇒ aucune suggestion.
     */
    @Transactional(readOnly = true)
    public List<SearchSuggestionDto> suggest(String q, int limit) {
        if (q == null || q.isBlank() || limit <= 0) {
            return List.of();
        }
        List<SearchSuggestionDto> suggestions = new ArrayList<>();
        Set<String> seen = new LinkedHashSet<>();
        Pageable cap = PageRequest.of(0, limit);

        for (Category category : categoryRepository.findByNameContainingIgnoreCaseOrderByNameAsc(q, cap)) {
            add(suggestions, seen, limit, "CATEGORY", category.getName(), category.getSlug());
        }
        for (String name : searchRepository.suggestMetierNames(q, cap)) {
            add(suggestions, seen, limit, "METIER", name, name);
        }
        for (String name : searchRepository.suggestServiceNames(q, cap)) {
            add(suggestions, seen, limit, "SERVICE", name, name);
        }
        return suggestions;
    }

    /** Ajoute une suggestion si le libellé n'a pas déjà été retenu et que la limite n'est pas atteinte. */
    private void add(
            List<SearchSuggestionDto> out, Set<String> seen, int limit, String type, String label, String value) {
        if (label == null || out.size() >= limit || !seen.add(label.toLowerCase())) {
            return;
        }
        out.add(new SearchSuggestionDto(type, label, value));
    }

    private Double distanceFor(Metier metier, SearchCriteria criteria) {
        if (criteria.lat() == null
                || criteria.lng() == null
                || metier.getGpsLat() == null
                || metier.getGpsLng() == null) {
            return null;
        }
        return GeoUtils.haversineKm(
                criteria.lat(), criteria.lng(), metier.getGpsLat().doubleValue(), metier.getGpsLng().doubleValue());
    }
}
