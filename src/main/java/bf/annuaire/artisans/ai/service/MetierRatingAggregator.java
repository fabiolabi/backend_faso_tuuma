package bf.annuaire.artisans.ai.service;

import bf.annuaire.artisans.client.rating.entity.MetierRating;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class MetierRatingAggregator {

    public record Aggregate(BigDecimal ratingAvg, int ratingCount) {}

    public Aggregate compute(List<MetierRating> approved) {
        double weightedSum = 0.0;
        double weightTotal = 0.0;
        for (MetierRating r : approved) {
            double weight = r.getWeight() != null ? r.getWeight().doubleValue() : 1.0;
            if (weight <= 0.0) {
                continue;
            }
            double score = r.getAiRating() != null ? r.getAiRating() : r.getStarRating();
            weightedSum += weight * score;
            weightTotal += weight;
        }
        BigDecimal avg = weightTotal == 0.0
                ? BigDecimal.ZERO.setScale(1)
                : BigDecimal.valueOf(weightedSum / weightTotal).setScale(1, RoundingMode.HALF_UP);
        return new Aggregate(avg, approved.size());
    }
}
