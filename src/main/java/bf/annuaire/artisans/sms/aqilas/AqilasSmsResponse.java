package bf.annuaire.artisans.sms.aqilas;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/** Réponse succès {@code POST /sms} — AQILAS API v1. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record AqilasSmsResponse(
        boolean success,
        String message,
        @JsonProperty("bulk_id") String bulkId,
        Integer cost,
        String currency) {}
