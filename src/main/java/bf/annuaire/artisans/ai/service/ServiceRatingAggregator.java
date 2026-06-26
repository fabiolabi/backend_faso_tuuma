package bf.annuaire.artisans.ai.service;

import bf.annuaire.artisans.client.rating.entity.ServiceRating;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * Calcul de la note agrégée d'une prestation — <strong>logique pure, sans I/O</strong> (testable
 * unitairement). La note est une <strong>moyenne pondérée</strong> du score de chaque avis approuvé
 * (poids {@code weight} ∈ [0,1], typiquement réduit en cas de décalage note/texte ; les avis
 * faux/inappropriés ont déjà été écartés via le statut {@code REJECTED}). Le score retenu par avis est
 * la note dérivée du texte ({@code aiRating}) si disponible, sinon l'étoile saisie.
 */
@Component
public class ServiceRatingAggregator {

    /** Résultat de l'agrégation : note arrondie au dixième et nombre d'avis pris en compte. */
    public record Aggregate(BigDecimal ratingAvg, int ratingCount) {}

    /** Agrège une liste d'avis <em>déjà filtrés</em> sur le statut {@code APPROVED}. */
    public Aggregate compute(List<ServiceRating> approved) {
        double weightedSum = 0.0;
        double weightTotal = 0.0;
        for (ServiceRating r : approved) {
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
