package bf.annuaire.artisans.ai.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration de la feature {@code ai} (notation automatique + recherche sémantique).
 *
 * <p>{@code enabled=false} (défaut dev/test) : aucun appel réseau n'est fait, un client heuristique de
 * repli prend le relais (cf. {@code HeuristicAiClient}). En prod, {@code enabled=true} + une clé
 * {@code GEMINI_API_KEY} activent les appels Gemini réels.
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "app.ai")
public class AiProperties {

    /** Active les appels Gemini réels. Faux ⇒ repli heuristique sans réseau. */
    private boolean enabled = false;

    private final Gemini gemini = new Gemini();

    @Getter
    @Setter
    public static class Gemini {
        /** Clé API Gemini (variable d'environnement {@code GEMINI_API_KEY}). Jamais commitée. */
        private String apiKey;

        private String baseUrl = "https://generativelanguage.googleapis.com/v1beta";

        /** Modèle de génération (analyse d'avis, synthèse). */
        private String chatModel = "gemini-2.0-flash";

        /** Modèle d'embedding (recherche sémantique). */
        private String embeddingModel = "text-embedding-004";
    }
}
