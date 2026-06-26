package bf.annuaire.artisans.search.service;

import bf.annuaire.artisans.ai.client.GeminiClient;
import bf.annuaire.artisans.ai.util.Vectors;
import bf.annuaire.artisans.common.util.GeoUtils;
import bf.annuaire.artisans.metier.dto.MetierSummaryDto;
import bf.annuaire.artisans.metier.entity.Metier;
import bf.annuaire.artisans.metier.mapper.MetierMapper;
import bf.annuaire.artisans.metier.repository.MetierRepository;
import bf.annuaire.artisans.search.dto.SearchCriteria;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Recherche sémantique : classe les enseignes par similarité de sens entre la requête et leur
 * embedding (cosinus calculé en mémoire — portable H2/PostgreSQL, sans index vectoriel). Repli
 * automatique sur la recherche mots-clés ({@link SearchService}) si l'IA est désactivée, si la
 * requête est vide, si l'embedding de requête est indisponible ou si aucune enseigne n'est encore
 * indexée.
 */
@Service
@RequiredArgsConstructor
public class SemanticSearchService {

    /** Plafond de candidats scorés en mémoire (échelle hackathon). */
    private static final int CANDIDATE_CAP = 200;

    private final GeminiClient ai;
    private final MetierRepository metierRepository;
    private final MetierMapper metierMapper;
    private final SearchService searchService;

    private record Scored(Metier metier, double score) {}

    @Transactional(readOnly = true)
    public Page<MetierSummaryDto> search(SearchCriteria criteria, Pageable pageable) {
        String q = criteria.q();
        if (!ai.isEnabled() || q == null || q.isBlank()) {
            return searchService.search(criteria, pageable);
        }
        float[] queryVector = ai.embed(q);
        if (queryVector == null) {
            return searchService.search(criteria, pageable);
        }
        List<Metier> candidates = metierRepository.findSemanticCandidates(PageRequest.of(0, CANDIDATE_CAP));
        if (candidates.isEmpty()) {
            return searchService.search(criteria, pageable);
        }

        List<Scored> scored = new ArrayList<>(candidates.size());
        for (Metier metier : candidates) {
            double score = Vectors.cosine(queryVector, Vectors.parse(metier.getSearchEmbedding()));
            scored.add(new Scored(metier, score));
        }
        scored.sort(Comparator.comparingDouble(Scored::score).reversed());

        int from = (int) Math.min(pageable.getOffset(), scored.size());
        int to = Math.min(from + pageable.getPageSize(), scored.size());
        List<MetierSummaryDto> content = scored.subList(from, to).stream()
                .map(s -> metierMapper.toSummary(s.metier(), distanceFor(s.metier(), criteria)))
                .toList();
        return new PageImpl<>(content, pageable, scored.size());
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
