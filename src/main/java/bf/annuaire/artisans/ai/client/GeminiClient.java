package bf.annuaire.artisans.ai.client;

import java.util.List;

/**
 * Abstraction des appels au modèle d'IA. Deux implémentations sélectionnées par
 * {@code app.ai.enabled} : {@code GeminiRestClient} (appels Gemini réels) et {@code HeuristicAiClient}
 * (repli sans réseau, pour le dev/les tests sans clé). Les implémentations ne lèvent jamais : en cas
 * d'échec, elles renvoient une valeur de repli neutre.
 */
public interface GeminiClient {

    /** Analyse un avis (texte + note étoilée saisie) et renvoie les signaux bruts. */
    ReviewAssessment analyze(String comment, int starRating);

    /** Génère une synthèse courte (FR) des avis approuvés d'une prestation, ou {@code null}. */
    String summarize(String serviceName, List<String> approvedComments);

    /** Calcule l'embedding sémantique d'un texte, ou {@code null} si indisponible. */
    float[] embed(String text);

    /** Indique si les appels IA réels sont actifs (false ⇒ repli heuristique). */
    boolean isEnabled();
}
