package bf.annuaire.artisans.sms.sender;

import bf.annuaire.artisans.sms.model.SmsPersonalizedMessage;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

/** Repli sans réseau : journalise le SMS (code OTP visible en dev). */
@Slf4j
public class NoOpSmsSender implements SmsSender {

    @Override
    public void sendMany(List<String> phonesE164, String message) {
        log.info("[sms:noop:/sms] → {} : {}", phonesE164, message);
    }

    @Override
    public void sendPersonalized(List<SmsPersonalizedMessage> messages) {
        log.info("[sms:noop:/bulksms] → {}", messages);
    }
}
