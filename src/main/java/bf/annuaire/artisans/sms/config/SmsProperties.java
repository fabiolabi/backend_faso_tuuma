package bf.annuaire.artisans.sms.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/** Configuration SMS (AQILAS) — préfixe {@code app.sms}. */
@Getter
@Setter
@ConfigurationProperties(prefix = "app.sms")
public class SmsProperties {

    /** Active l'envoi réel via AQILAS. Faux ⇒ repli NoOp (code journalisé). */
    private boolean enabled = false;

    private final Aqilas aqilas = new Aqilas();

    @Getter
    @Setter
    public static class Aqilas {
        private String baseUrl = "https://www.aqilas.com/api/v1";
        private String apiToken = "";
        /** Sender ID validé dans le dashboard AQILAS. */
        private String senderId = "";
    }
}
