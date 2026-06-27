package bf.annuaire.artisans.sms.aqilas;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * Corps JSON {@code POST /sms} — AQILAS API v1.
 *
 * @param from Sender ID validé dans le dashboard AQILAS
 * @param text Message (minimum 4 caractères)
 * @param to Numéros destinataires au format international E.164
 * @param sendAt Programmation optionnelle, format {@code HH:mm ddmmyyyy}
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record AqilasSmsRequest(
        String from,
        String text,
        List<String> to,
        @JsonProperty("send_at") String sendAt) {

    public static AqilasSmsRequest immediate(String from, String text, List<String> to) {
        return new AqilasSmsRequest(from, text, to, null);
    }
}
