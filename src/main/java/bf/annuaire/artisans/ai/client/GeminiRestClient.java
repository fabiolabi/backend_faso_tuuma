package bf.annuaire.artisans.ai.client;

import bf.annuaire.artisans.ai.config.AiProperties;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/**
 * Implémentation réelle s'appuyant sur l'API Gemini (REST), active quand {@code app.ai.enabled=true}.
 * Aucune méthode ne lève : en cas d'erreur réseau / parsing, on journalise et on renvoie une valeur de
 * repli neutre, de sorte qu'un avis reste enregistré et que le traitement asynchrone ne casse pas.
 */
@Slf4j
@Component
@ConditionalOnProperty(prefix = "app.ai", name = "enabled", havingValue = "true")
public class GeminiRestClient implements GeminiClient {

    private final RestClient gemini;
    private final ObjectMapper mapper;
    private final AiProperties properties;

    public GeminiRestClient(
            @Qualifier("geminiHttpClient") RestClient gemini, ObjectMapper mapper, AiProperties properties) {
        this.gemini = gemini;
        this.mapper = mapper;
        this.properties = properties;
    }

    @Override
    public ReviewAssessment analyze(String comment, int starRating) {
        String prompt =
                """
                Tu es un modérateur d'avis pour un annuaire d'artisans au Burkina Faso.
                Analyse l'avis client ci-dessous (rédigé en français, parfois mêlé d'argot local).
                Réponds STRICTEMENT en JSON, sans texte autour, avec ce schéma :
                {"textRating": <entier 1-5 que le SENS du texte justifie, indépendamment de la note saisie>,
                 "sentiment": "POSITIVE" | "NEUTRAL" | "NEGATIVE",
                 "sentimentScore": <nombre entre -1 et 1>,
                 "fake": <true si l'avis semble faux, spam, générique ou abusif>,
                 "inappropriate": <true si l'avis est grossier, injurieux ou inapproprié>,
                 "reason": "<courte justification en français, ou chaîne vide>"}

                Note étoilée saisie par le client : %d/5
                Avis : "%s"
                """
                        .formatted(starRating, safe(comment));
        try {
            String json = generateContent(prompt);
            JsonNode node = mapper.readTree(stripFences(json));
            int textRating = clamp(node.path("textRating").asInt(starRating));
            String label = node.path("sentiment").asText("NEUTRAL");
            double score = node.path("sentimentScore").asDouble(0.0);
            boolean fake = node.path("fake").asBoolean(false);
            boolean inappropriate = node.path("inappropriate").asBoolean(false);
            String reason = node.path("reason").asText(null);
            return new ReviewAssessment(textRating, label, score, fake, inappropriate, blankToNull(reason));
        } catch (Exception e) {
            log.warn("Analyse Gemini échouée, repli neutre : {}", e.getMessage());
            return new ReviewAssessment(clamp(starRating), "NEUTRAL", 0.0, false, false, null);
        }
    }

    @Override
    public String summarize(String serviceName, List<String> approvedComments) {
        if (approvedComments == null || approvedComments.isEmpty()) {
            return null;
        }
        String joined = String.join("\n- ", approvedComments);
        String prompt =
                """
                Voici les avis clients d'une prestation « %s » d'un artisan.
                Rédige en français une synthèse de 1 à 2 phrases (points forts et points faibles),
                neutre et factuelle, sans introduction ni liste. Réponds uniquement par la synthèse.

                Avis :
                - %s
                """
                        .formatted(safe(serviceName), joined);
        try {
            String text = generateContent(prompt);
            return blankToNull(stripFences(text));
        } catch (Exception e) {
            log.warn("Synthèse Gemini échouée : {}", e.getMessage());
            return null;
        }
    }

    @Override
    public float[] embed(String text) {
        if (text == null || text.isBlank()) {
            return null;
        }
        try {
            String embeddingModel = properties.getGemini().getEmbeddingModel();
            Map<String, Object> body = Map.of(
                    "model", "models/" + embeddingModel,
                    "content", Map.of("parts", List.of(Map.of("text", text))));
            JsonNode response = gemini.post()
                    .uri("/models/{model}:embedContent?key={key}", embeddingModel, properties.getGemini().getApiKey())
                    .body(body)
                    .retrieve()
                    .body(JsonNode.class);
            JsonNode values = response == null ? null : response.path("embedding").path("values");
            if (values == null || !values.isArray() || values.isEmpty()) {
                return null;
            }
            float[] vector = new float[values.size()];
            for (int i = 0; i < values.size(); i++) {
                vector[i] = (float) values.get(i).asDouble();
            }
            return vector;
        } catch (Exception e) {
            log.warn("Embedding Gemini échoué : {}", e.getMessage());
            return null;
        }
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    // ----------------------------------------------------------------- Helpers Gemini

    /** Appel {@code generateContent}, renvoie le texte de la première réponse. */
    private String generateContent(String prompt) {
        Map<String, Object> body = Map.of(
                "contents", List.of(Map.of("parts", List.of(Map.of("text", prompt)))),
                "generationConfig", Map.of("temperature", 0));
        JsonNode response = gemini.post()
                .uri("/models/{model}:generateContent?key={key}",
                        properties.getGemini().getChatModel(), properties.getGemini().getApiKey())
                .body(body)
                .retrieve()
                .body(JsonNode.class);
        if (response == null) {
            throw new IllegalStateException("réponse vide");
        }
        return response.path("candidates").path(0).path("content").path("parts").path(0).path("text").asText("");
    }

    private String stripFences(String text) {
        if (text == null) {
            return "";
        }
        return text.replaceAll("(?s)```(?:json)?", "").trim();
    }

    private String safe(String s) {
        return s == null ? "" : s.replace("\"", "'");
    }

    private String blankToNull(String s) {
        return s == null || s.isBlank() ? null : s;
    }

    private int clamp(int rating) {
        return Math.max(1, Math.min(5, rating));
    }
}
