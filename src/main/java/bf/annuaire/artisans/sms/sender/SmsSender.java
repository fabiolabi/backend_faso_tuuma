package bf.annuaire.artisans.sms.sender;

import bf.annuaire.artisans.sms.model.SmsPersonalizedMessage;
import java.util.List;

/** Abstraction d'envoi SMS (AQILAS en prod, NoOp en dev). */
public interface SmsSender {

    /** Envoie un SMS à un seul destinataire (E.164, ex. {@code +22670123456}). */
    default void send(String phoneE164, String message) {
        sendMany(List.of(phoneE164), message);
    }

    /**
     * Envoie le même message à un ou plusieurs numéros ({@code POST /sms}, champ {@code to}).
     */
    void sendMany(List<String> phonesE164, String message);

    /**
     * Envoie des messages différents à chaque destinataire ({@code POST /bulksms}).
     */
    void sendPersonalized(List<SmsPersonalizedMessage> messages);
}
