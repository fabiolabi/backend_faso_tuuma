package bf.annuaire.artisans.ai.event;

import bf.annuaire.artisans.ai.service.EmbeddingService;
import bf.annuaire.artisans.ai.service.MetierRatingRecalculator;
import bf.annuaire.artisans.ai.service.ReviewAnalysisService;
import bf.annuaire.artisans.ai.service.ServiceRatingRecalculator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Orchestration asynchrone des traitements IA. Les écouteurs se déclenchent <strong>après le commit</strong>
 * de la transaction émettrice (l'avis / le contenu est déjà persisté) et s'exécutent sur le pool
 * {@code aiExecutor}, afin de ne jamais ralentir la requête HTTP du client mobile. Chaque traitement
 * ouvre sa propre transaction et journalise sans relancer en cas d'échec.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AiEventListener {

    private final ReviewAnalysisService analysisService;
    private final ServiceRatingRecalculator recalculator;
    private final MetierRatingRecalculator metierRecalculator;
    private final EmbeddingService embeddingService;

    /** Nouvel avis commerce : analyse IA puis recalcul de la note de l'enseigne. */
    @Async("aiExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onMetierReviewSubmitted(MetierReviewSubmittedEvent event) {
        try {
            analysisService.analyzeMetier(event.ratingId());
            metierRecalculator.recalc(event.metierId());
        } catch (Exception e) {
            log.warn("Traitement IA de l'avis commerce {} échoué : {}", event.ratingId(), e.getMessage());
        }
    }

    /** Avis commerce supprimé / modifié : recalcul de la note de l'enseigne. */
    @Async("aiExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onMetierAggregateChanged(MetierAggregateChangedEvent event) {
        try {
            metierRecalculator.recalc(event.metierId());
        } catch (Exception e) {
            log.warn("Recalcul de la note du commerce {} échoué : {}", event.metierId(), e.getMessage());
        }
    }

    /** Nouvel avis prestation : analyse IA de l'avis, puis recalcul de la note + synthèse du service. */
    @Async("aiExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onReviewSubmitted(ReviewSubmittedEvent event) {
        try {
            analysisService.analyze(event.ratingId());
            recalculator.recalc(event.serviceId());
        } catch (Exception e) {
            log.warn("Traitement IA de l'avis {} échoué : {}", event.ratingId(), e.getMessage());
        }
    }

    /** Avis supprimé / lot modifié : recalcul de la note + synthèse du service. */
    @Async("aiExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onAggregateChanged(ServiceAggregateChangedEvent event) {
        try {
            recalculator.recalc(event.serviceId());
        } catch (Exception e) {
            log.warn("Recalcul de la note du service {} échoué : {}", event.serviceId(), e.getMessage());
        }
    }

    /** Contenu d'enseigne modifié : recalcul de l'embedding de recherche sémantique. */
    @Async("aiExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onMetierContentChanged(MetierContentChangedEvent event) {
        try {
            embeddingService.recompute(event.metierId());
        } catch (Exception e) {
            log.warn("Recalcul de l'embedding du metier {} échoué : {}", event.metierId(), e.getMessage());
        }
    }
}
