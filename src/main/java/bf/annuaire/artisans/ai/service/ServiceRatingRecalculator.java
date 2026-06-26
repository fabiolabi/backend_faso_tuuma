package bf.annuaire.artisans.ai.service;

import bf.annuaire.artisans.ai.client.GeminiClient;
import bf.annuaire.artisans.ai.service.ServiceRatingAggregator.Aggregate;
import bf.annuaire.artisans.client.rating.entity.RatingStatus;
import bf.annuaire.artisans.client.rating.entity.ServiceRating;
import bf.annuaire.artisans.client.rating.repository.ServiceRatingRepository;
import bf.annuaire.artisans.metier.entity.Service;
import bf.annuaire.artisans.metier.repository.ServiceRepository;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

/**
 * Recalcule la note dénormalisée d'une prestation ({@code rating_avg} / {@code rating_count}) à partir
 * de ses avis approuvés, puis régénère la synthèse d'avis ({@code ai_summary}). Appelé en asynchrone
 * (après commit) à chaque changement d'avis. La synthèse n'est disponible que si l'IA est active.
 */
@org.springframework.stereotype.Service
@RequiredArgsConstructor
public class ServiceRatingRecalculator {

    /** Nombre maximal d'avis envoyés au modèle pour la synthèse (maîtrise du coût). */
    private static final int SUMMARY_SAMPLE = 50;

    private final ServiceRepository serviceRepository;
    private final ServiceRatingRepository ratingRepository;
    private final ServiceRatingAggregator aggregator;
    private final GeminiClient ai;

    @Transactional
    public void recalc(Long serviceId) {
        Service service = serviceRepository.findById(serviceId).orElse(null);
        if (service == null) {
            return;
        }
        List<ServiceRating> approved = ratingRepository.findByServiceIdAndStatus(serviceId, RatingStatus.APPROVED);

        Aggregate aggregate = aggregator.compute(approved);
        service.setRatingAvg(aggregate.ratingAvg());
        service.setRatingCount(aggregate.ratingCount());

        List<String> comments = approved.stream()
                .map(ServiceRating::getComment)
                .filter(c -> c != null && !c.isBlank())
                .limit(SUMMARY_SAMPLE)
                .toList();
        service.setAiSummary(ai.summarize(service.getName(), comments));
        service.setAiSummaryUpdatedAt(Instant.now());

        serviceRepository.save(service);
    }
}
