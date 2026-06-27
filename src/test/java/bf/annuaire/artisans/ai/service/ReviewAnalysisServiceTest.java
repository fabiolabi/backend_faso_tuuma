package bf.annuaire.artisans.ai.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import bf.annuaire.artisans.ai.client.GeminiClient;
import bf.annuaire.artisans.ai.client.HeuristicAiClient;
import bf.annuaire.artisans.ai.client.ReviewAssessment;
import bf.annuaire.artisans.client.rating.entity.RatingStatus;
import bf.annuaire.artisans.client.rating.entity.ServiceRating;
import bf.annuaire.artisans.client.rating.repository.MetierRatingRepository;
import bf.annuaire.artisans.client.rating.repository.ServiceRatingRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;

/**
 * Vérifie la décision déterministe de {@link ReviewAnalysisService} (pondération, statut, décalage)
 * en aval des signaux du modèle — avec le repli heuristique et avec un client IA simulé.
 */
class ReviewAnalysisServiceTest {

    private final ServiceRatingRepository repository = mock(ServiceRatingRepository.class);
    private final MetierRatingRepository metierRatingRepository = mock(MetierRatingRepository.class);

    private ReviewAnalysisService service(GeminiClient client) {
        return new ReviewAnalysisService(client, repository, metierRatingRepository);
    }

    private ServiceRating persisted(ServiceRating rating) {
        when(repository.findById(1L)).thenReturn(Optional.of(rating));
        when(repository.save(any(ServiceRating.class))).thenAnswer(i -> i.getArgument(0));
        return rating;
    }

    private static ServiceRating rating(int star, String comment) {
        ServiceRating r = new ServiceRating();
        r.setStarRating((short) star);
        r.setComment(comment);
        return r;
    }

    @Test
    void profanityIsRejectedAndExcludedFromTheNote() {
        ServiceRating r = persisted(rating(5, "travail de con, vraiment nul"));
        service(new HeuristicAiClient()).analyze(1L);

        assertThat(r.isInappropriate()).isTrue();
        assertThat(r.getStatus()).isEqualTo(RatingStatus.REJECTED);
        assertThat(r.getWeight()).isEqualByComparingTo("0.00");
        assertThat(r.getAiProcessedAt()).isNotNull();
    }

    @Test
    void cleanReviewIsApprovedWithFullWeight() {
        ServiceRating r = persisted(rating(5, "très bon travail, je recommande"));
        service(new HeuristicAiClient()).analyze(1L);

        assertThat(r.getStatus()).isEqualTo(RatingStatus.APPROVED);
        assertThat(r.isMismatch()).isFalse();
        assertThat(r.getWeight()).isEqualByComparingTo("1.00");
    }

    @Test
    void noteTextMismatchIsFlaggedAndDownweighted() {
        GeminiClient contradicting = mock(GeminiClient.class);
        when(contradicting.analyze(any(), org.mockito.ArgumentMatchers.anyInt()))
                .thenReturn(new ReviewAssessment(1, "NEGATIVE", -0.9, false, false, "texte très négatif"));
        ServiceRating r = persisted(rating(5, "c'était horrible, à éviter"));

        service(contradicting).analyze(1L);

        assertThat(r.isMismatch()).isTrue();
        assertThat(r.getAiRating()).isEqualTo((short) 1);
        assertThat(r.getStatus()).isEqualTo(RatingStatus.APPROVED);
        assertThat(r.getWeight()).isEqualByComparingTo("0.40");
    }

    @Test
    void blankCommentApprovedWithStarAsAiRating() {
        ServiceRating r = persisted(rating(4, "   "));
        service(new HeuristicAiClient()).analyze(1L);

        assertThat(r.getStatus()).isEqualTo(RatingStatus.APPROVED);
        assertThat(r.getAiRating()).isEqualTo((short) 4);
        assertThat(r.getWeight()).isEqualByComparingTo("1.00");
    }
}
