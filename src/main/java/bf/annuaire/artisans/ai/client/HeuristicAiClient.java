package bf.annuaire.artisans.ai.client;

import java.util.List;
import java.util.Set;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Repli sans réseau, actif quand {@code app.ai.enabled=false} (défaut dev/test). Permet à
 * l'application de démarrer, de passer les tests et de faire une démo sans clé Gemini :
 *
 * <ul>
 *   <li>la note dérivée du texte = la note étoilée saisie (pas d'analyse de sentiment fine) ;
 *   <li>une détection de grossièreté basique (liste de mots) reste fonctionnelle ;
 *   <li>pas de synthèse ni d'embedding ⇒ la recherche sémantique retombe sur les mots-clés.
 * </ul>
 */
@Component
@ConditionalOnProperty(prefix = "app.ai", name = "enabled", havingValue = "false", matchIfMissing = true)
public class HeuristicAiClient implements GeminiClient {

    /** Petite liste indicative de termes grossiers (FR/EN), suffisante pour une démo hors-ligne. */
    private static final Set<String> PROFANITY = Set.of(
            "con", "connard", "connasse", "salaud", "salope", "merde", "merdique", "pute", "putain",
            "encule", "enculé", "batard", "bâtard", "ordure", "fdp", "ntm", "fuck", "shit", "asshole",
            "bitch", "bastard", "idiot", "imbecile", "imbécile", "crétin", "cretin");

    @Override
    public ReviewAssessment analyze(String comment, int starRating) {
        boolean inappropriate = containsProfanity(comment);
        int rating = clamp(starRating);
        String label = rating >= 4 ? "POSITIVE" : (rating == 3 ? "NEUTRAL" : "NEGATIVE");
        double score = (rating - 3) / 2.0; // 1->-1.0, 3->0.0, 5->1.0
        String reason = inappropriate ? "Contenu inapproprié détecté (filtre local)." : null;
        return new ReviewAssessment(rating, label, score, false, inappropriate, reason);
    }

    @Override
    public String summarize(String serviceName, List<String> approvedComments) {
        return null; // pas de synthèse hors-ligne
    }

    @Override
    public float[] embed(String text) {
        return null; // pas d'embedding hors-ligne ⇒ fallback recherche mots-clés
    }

    @Override
    public boolean isEnabled() {
        return false;
    }

    private boolean containsProfanity(String comment) {
        if (comment == null || comment.isBlank()) {
            return false;
        }
        for (String token : comment.toLowerCase().split("[^\\p{L}]+")) {
            if (PROFANITY.contains(token)) {
                return true;
            }
        }
        return false;
    }

    private int clamp(int rating) {
        return Math.max(1, Math.min(5, rating));
    }
}
