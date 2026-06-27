package bf.annuaire.artisans.sms.sender;

import bf.annuaire.artisans.sms.aqilas.AqilasBulkMessage;
import bf.annuaire.artisans.sms.aqilas.AqilasBulkSmsRequest;
import bf.annuaire.artisans.sms.aqilas.AqilasSmsRequest;
import bf.annuaire.artisans.sms.aqilas.AqilasSmsResponse;
import bf.annuaire.artisans.sms.config.SmsProperties;
import bf.annuaire.artisans.sms.model.SmsPersonalizedMessage;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

/**
 * Envoi SMS via l'API REST AQILAS v1.
 *
 * <ul>
 *   <li>{@code POST /sms} — même message, plusieurs destinataires ({@code to[]})
 *   <li>{@code POST /bulksms} — message personnalisé par destinataire ({@code messages[]})
 * </ul>
 */
@Slf4j
public class AqilasSmsSender implements SmsSender {

    private static final int MIN_TEXT_LENGTH = 4;

    private final RestClient client;
    private final SmsProperties properties;
    private final ObjectMapper objectMapper;

    public AqilasSmsSender(RestClient aqilasRestClient, SmsProperties properties) {
        this(aqilasRestClient, properties, new ObjectMapper());
    }

    AqilasSmsSender(RestClient aqilasRestClient, SmsProperties properties, ObjectMapper objectMapper) {
        this.client = aqilasRestClient;
        this.properties = properties;
        this.objectMapper = objectMapper;
    }

    @Override
    public void sendMany(List<String> phonesE164, String message) {
        List<String> recipients = normalizeRecipients(phonesE164);
        validateMessage(message);

        AqilasCredentials credentials = requireCredentials();
        AqilasSmsRequest body =
                AqilasSmsRequest.immediate(credentials.senderId(), message, recipients);

        AqilasSmsResponse response = post(credentials.token(), "/sms", body, recipients);
        log.info(
                "SMS AQILAS (/sms) → {} destinataire(s), bulkId={}, coût={} {}",
                recipients.size(),
                response.bulkId(),
                response.cost(),
                response.currency());
    }

    @Override
    public void sendPersonalized(List<SmsPersonalizedMessage> messages) {
        List<AqilasBulkMessage> payload = normalizePersonalized(messages);
        AqilasCredentials credentials = requireCredentials();
        AqilasBulkSmsRequest body = new AqilasBulkSmsRequest(credentials.senderId(), payload);

        AqilasSmsResponse response =
                post(credentials.token(), "/bulksms", body, payload.size() + " message(s)");
        log.info(
                "SMS AQILAS (/bulksms) → {} message(s), bulkId={}, coût={} {}",
                payload.size(),
                response.bulkId(),
                response.cost(),
                response.currency());
    }

    private AqilasSmsResponse post(String token, String path, Object body, Object logContext) {
        try {
            AqilasSmsResponse response = client.post()
                    .uri(path)
                    .header("X-AUTH-TOKEN", token)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .body(AqilasSmsResponse.class);

            if (response == null || !response.success()) {
                throw new IllegalStateException(
                        response == null
                                ? "Réponse AQILAS vide."
                                : "AQILAS a refusé l'envoi : " + response.message());
            }
            return response;
        } catch (RestClientResponseException e) {
            String detail = extractErrorMessage(e.getResponseBodyAsString());
            log.error(
                    "Échec envoi SMS AQILAS {} → {} (HTTP {}): {}",
                    path,
                    logContext,
                    e.getStatusCode().value(),
                    detail);
            throw new IllegalStateException(mapUserMessage(e.getStatusCode().value(), detail), e);
        } catch (IllegalStateException e) {
            throw e;
        } catch (Exception e) {
            log.error("Échec envoi SMS AQILAS {} → {} : {}", path, logContext, e.getMessage(), e);
            throw new IllegalStateException("Envoi SMS impossible.", e);
        }
    }

    private AqilasCredentials requireCredentials() {
        String token = properties.getAqilas().getApiToken();
        String senderId = properties.getAqilas().getSenderId();
        if (!StringUtils.hasText(token) || !StringUtils.hasText(senderId)) {
            throw new IllegalStateException(
                    "AQILAS non configuré : AQILAS_API_TOKEN et AQILAS_SENDER_ID requis.");
        }
        return new AqilasCredentials(token, senderId);
    }

    private static List<String> normalizeRecipients(List<String> phonesE164) {
        if (phonesE164 == null || phonesE164.isEmpty()) {
            throw new IllegalArgumentException("Au moins un numéro destinataire requis.");
        }
        Set<String> unique = new LinkedHashSet<>();
        for (String raw : phonesE164) {
            if (!StringUtils.hasText(raw)) {
                continue;
            }
            unique.add(requireE164(raw.trim()));
        }
        if (unique.isEmpty()) {
            throw new IllegalArgumentException("Au moins un numéro destinataire requis.");
        }
        return new ArrayList<>(unique);
    }

    private static List<AqilasBulkMessage> normalizePersonalized(List<SmsPersonalizedMessage> messages) {
        if (messages == null || messages.isEmpty()) {
            throw new IllegalArgumentException("Au moins un message personnalisé requis.");
        }
        List<AqilasBulkMessage> payload = new ArrayList<>();
        for (SmsPersonalizedMessage item : messages) {
            if (item == null) {
                continue;
            }
            String phone = requireE164(item.phoneE164());
            validateMessage(item.text());
            payload.add(new AqilasBulkMessage(phone, item.text().trim()));
        }
        if (payload.isEmpty()) {
            throw new IllegalArgumentException("Au moins un message personnalisé requis.");
        }
        return payload;
    }

    private static String requireE164(String phone) {
        if (!StringUtils.hasText(phone) || !phone.startsWith("+")) {
            throw new IllegalArgumentException("Numéro AQILAS invalide (E.164 requis) : " + phone);
        }
        return phone.trim();
    }

    private static void validateMessage(String message) {
        if (!StringUtils.hasText(message) || message.trim().length() < MIN_TEXT_LENGTH) {
            throw new IllegalArgumentException(
                    "Le message SMS doit contenir au moins " + MIN_TEXT_LENGTH + " caractères.");
        }
    }

    private String extractErrorMessage(String body) {
        if (!StringUtils.hasText(body)) {
            return "";
        }
        try {
            JsonNode node = objectMapper.readTree(body);
            if (node.hasNonNull("message")) {
                return node.get("message").asText();
            }
            if (node.hasNonNull("error")) {
                return node.get("error").asText();
            }
        } catch (Exception ignored) {
            // corps non JSON
        }
        return body.trim();
    }

    private static String mapUserMessage(int status, String detail) {
        String lower = detail == null ? "" : detail.toLowerCase();
        if (status == 404 || lower.contains("mot clé") || lower.contains("sender")) {
            return "Configuration SMS invalide (Sender ID). Contactez le support.";
        }
        if (lower.contains("credit")) {
            return "Service SMS temporairement indisponible.";
        }
        if (lower.contains("contact") && lower.contains("invalide")) {
            return "Numéro de téléphone invalide.";
        }
        if (lower.contains("verifier vos données")) {
            return "Données SMS invalides. Vérifiez le numéro et réessayez.";
        }
        return "Impossible d'envoyer le SMS.";
    }

    private record AqilasCredentials(String token, String senderId) {}
}
