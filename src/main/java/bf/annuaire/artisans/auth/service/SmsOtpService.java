package bf.annuaire.artisans.auth.service;

import bf.annuaire.artisans.common.util.PhoneUtils;
import bf.annuaire.artisans.sms.sender.SmsSender;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/** Envoi du code OTP de réinitialisation par SMS. */
@Service
@RequiredArgsConstructor
public class SmsOtpService {

    private final SmsSender smsSender;

    public void sendPasswordResetCode(String phone, String code, long ttlSeconds) {
        int minutes = Math.max(1, (int) (ttlSeconds / 60));
        String message =
                "Faso Tuuma : votre code de reinitialisation est %s. Valide %d min. Ne le partagez pas."
                        .formatted(code, minutes);
        smsSender.send(PhoneUtils.toE164(phone), message);
    }
}
