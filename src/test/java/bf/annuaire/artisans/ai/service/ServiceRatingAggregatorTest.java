package bf.annuaire.artisans.ai.service;

import static org.assertj.core.api.Assertions.assertThat;

import bf.annuaire.artisans.ai.service.ServiceRatingAggregator.Aggregate;
import bf.annuaire.artisans.client.rating.entity.ServiceRating;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;

/** Vérifie la moyenne pondérée pure de {@link ServiceRatingAggregator}. */
class ServiceRatingAggregatorTest {

    private final ServiceRatingAggregator aggregator = new ServiceRatingAggregator();

    private static ServiceRating rating(int aiRating, int starRating, String weight) {
        ServiceRating r = new ServiceRating();
        r.setStarRating((short) starRating);
        r.setAiRating((short) aiRating);
        r.setWeight(new BigDecimal(weight));
        return r;
    }

    @Test
    void emptyListGivesZeroAndCountZero() {
        Aggregate result = aggregator.compute(List.of());
        assertThat(result.ratingAvg()).isEqualByComparingTo("0.0");
        assertThat(result.ratingCount()).isZero();
    }

    @Test
    void plainMeanWhenAllWeightsEqualOne() {
        Aggregate result = aggregator.compute(List.of(rating(4, 4, "1.00"), rating(2, 2, "1.00")));
        assertThat(result.ratingAvg()).isEqualByComparingTo("3.0");
        assertThat(result.ratingCount()).isEqualTo(2);
    }

    @Test
    void mismatchedReviewIsDownweighted() {
        // (5*1.0 + 1*0.4) / 1.4 = 3.857 -> 3.9 ; les deux avis restent comptés.
        Aggregate result = aggregator.compute(List.of(rating(5, 5, "1.00"), rating(1, 5, "0.40")));
        assertThat(result.ratingAvg()).isEqualByComparingTo("3.9");
        assertThat(result.ratingCount()).isEqualTo(2);
    }

    @Test
    void fallsBackToStarRatingWhenAiRatingMissing() {
        ServiceRating r = new ServiceRating();
        r.setStarRating((short) 3);
        r.setWeight(new BigDecimal("1.00"));
        Aggregate result = aggregator.compute(List.of(r));
        assertThat(result.ratingAvg()).isEqualByComparingTo("3.0");
        assertThat(result.ratingCount()).isEqualTo(1);
    }
}
