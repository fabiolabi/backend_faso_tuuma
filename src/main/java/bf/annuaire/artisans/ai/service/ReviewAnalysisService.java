package bf.annuaire.artisans.ai.service;

import bf.annuaire.artisans.ai.client.GeminiClient;
import bf.annuaire.artisans.ai.client.ReviewAssessment;
import bf.annuaire.artisans.client.rating.entity.RatingStatus;
import bf.annuaire.artisans.client.rating.entity.ServiceRating;
import bf.annuaire.artisans.client.rating.repository.ServiceRatingRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Analyse IA d'un avis : sentiment, note dérivée du texte, détection de décalage note/texte, de faux
 * avis et de contenu inapproprié. Renseigne ensuite, de façon déterministe, la pondération et le
 * statut de modération. Appelé en asynchrone (après commit) par {@code AiEventListener}.
 */
@Service
@RequiredArgsConstructor
public class ReviewAnalysisService {

    /** Écart minimal entre note dérivée du texte et étoile saisie pour signaler un décalage. */
    private static final int MISMATCH_THRESHOLD = 2;

    /** Poids d'un avis en décalage note/texte (on le conserve mais on l'atténue). */
    private static final BigDecimal MISMATCH_WEIGHT = new BigDecimal("0.40");

    private final GeminiClient ai;
    private final ServiceRatingRepository ratingRepository;

    @Transactional
    public void analyze(Long ratingId) {
        ServiceRating rating = ratingRepository.findById(ratingId).orElse(null);
        if (rating == null) {
            return;
        }
        if (rating.getComment() == null || rating.getComment().isBlank()) {
            applyNoText(rating);
        } else {
            apply(rating, ai.analyze(rating.getComment(), rating.getStarRating()));
        }
        rating.setAiProcessedAt(Instant.now());
        ratingRepository.save(rating);
    }

    /** Avis sans texte : la note IA = l'étoile saisie, approuvé d'office, plein poids. */
    private void applyNoText(ServiceRating rating) {
        rating.setAiRating(rating.getStarRating());
        rating.setSentimentLabel(null);
        rating.setSentimentScore(null);
        rating.setMismatch(false);
        rating.setFake(false);
        rating.setInappropriate(false);
        rating.setModerationReason(null);
        rating.setWeight(BigDecimal.ONE.setScale(2));
        rating.setStatus(RatingStatus.APPROVED);
    }

    private void apply(ServiceRating rating, ReviewAssessment a) {
        boolean mismatch = Math.abs(a.textRating() - rating.getStarRating()) >= MISMATCH_THRESHOLD;
        boolean rejected = a.fake() || a.inappropriate();

        rating.setAiRating((short) clamp(a.textRating()));
        rating.setSentimentLabel(a.sentimentLabel());
        rating.setSentimentScore(clampScore(a.sentimentScore()));
        rating.setMismatch(mismatch);
        rating.setFake(a.fake());
        rating.setInappropriate(a.inappropriate());
        rating.setModerationReason(a.reason());
        rating.setStatus(rejected ? RatingStatus.REJECTED : RatingStatus.APPROVED);
        rating.setWeight(rejected
                ? BigDecimal.ZERO.setScale(2)
                : (mismatch ? MISMATCH_WEIGHT : BigDecimal.ONE.setScale(2)));
    }

    private int clamp(int rating) {
        return Math.max(1, Math.min(5, rating));
    }

    private BigDecimal clampScore(double score) {
        double bounded = Math.max(-1.0, Math.min(1.0, score));
        return BigDecimal.valueOf(bounded).setScale(2, RoundingMode.HALF_UP);
    }
}
