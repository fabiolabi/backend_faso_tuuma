package bf.annuaire.artisans.metier.service;

import bf.annuaire.artisans.metier.entity.Metier;
import bf.annuaire.artisans.metier.repository.MetierRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.stereotype.Service;

/**
 * Charge les enseignes pour l'affichage en liste : une requête avec {@code @EntityGraph} au lieu
 * d'un N+1 sur adresse, couverture et catégories après une recherche SQL native (IDs seulement).
 */
@Service
@RequiredArgsConstructor
public class MetierHydrationService {

    private final MetierRepository metierRepository;

    public List<Metier> hydrateSummaries(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        Map<Long, Metier> byId = metierRepository.findSummariesByIdIn(ids).stream()
                .collect(Collectors.toMap(Metier::getId, Function.identity()));
        List<Metier> ordered = new ArrayList<>(ids.size());
        for (Long id : ids) {
            Metier metier = byId.get(id);
            if (metier != null) {
                ordered.add(metier);
            }
        }
        return ordered;
    }

    public <T> Page<T> mapIdPage(Page<Long> idPage, Function<Metier, T> mapper) {
        List<T> content =
                hydrateSummaries(idPage.getContent()).stream().map(mapper).toList();
        return new PageImpl<>(content, idPage.getPageable(), idPage.getTotalElements());
    }
}
