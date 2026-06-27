package bf.annuaire.artisans.ai.service;

import bf.annuaire.artisans.ai.client.GeminiClient;
import bf.annuaire.artisans.ai.service.MetierRatingAggregator.Aggregate;
import bf.annuaire.artisans.client.rating.entity.MetierRating;
import bf.annuaire.artisans.client.rating.entity.RatingStatus;
import bf.annuaire.artisans.client.rating.repository.MetierRatingRepository;
import bf.annuaire.artisans.metier.entity.Metier;
import bf.annuaire.artisans.metier.repository.MetierRepository;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

@org.springframework.stereotype.Service
@RequiredArgsConstructor
public class MetierRatingRecalculator {

    private static final int SUMMARY_SAMPLE = 50;

    private final MetierRepository metierRepository;
    private final MetierRatingRepository ratingRepository;
    private final MetierRatingAggregator aggregator;
    private final GeminiClient ai;

    @Transactional
    public void recalc(Long metierId) {
        Metier metier = metierRepository.findById(metierId).orElse(null);
        if (metier == null) {
            return;
        }
        List<MetierRating> approved =
                ratingRepository.findByMetierIdAndStatus(metierId, RatingStatus.APPROVED);

        Aggregate aggregate = aggregator.compute(approved);
        metier.setRatingAvg(aggregate.ratingAvg());
        metier.setRatingCount(aggregate.ratingCount());

        List<String> comments = approved.stream()
                .map(MetierRating::getComment)
                .filter(c -> c != null && !c.isBlank())
                .limit(SUMMARY_SAMPLE)
                .toList();
        metier.setAiSummary(ai.summarize(metier.getName(), comments));
        metier.setAiSummaryUpdatedAt(Instant.now());

        metierRepository.save(metier);
    }
}
