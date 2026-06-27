package bf.annuaire.artisans.auth.service;

import bf.annuaire.artisans.auth.security.AuthProperties;
import bf.annuaire.artisans.common.exception.BadRequestException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * Envoi des emails transactionnels (code de réinitialisation de mot de passe).
 *
 * <p>En dev ({@code app.mail.enabled=false} ou aucun serveur SMTP), le code est écrit dans les logs
 * plutôt qu'envoyé, pour permettre le test du flux sans infrastructure mail.
 */
@Service
@Slf4j
public class MailService {

    private final ObjectProvider<JavaMailSender> mailSenderProvider;
    private final AuthProperties properties;

    public MailService(ObjectProvider<JavaMailSender> mailSenderProvider, AuthProperties properties) {
        this.mailSenderProvider = mailSenderProvider;
        this.properties = properties;
    }

    /** Envoie (ou logue en dev) le code de réinitialisation à 6 chiffres. */
    public void sendPasswordResetCode(String to, String code, long ttlSeconds) {
        int minutes = Math.max(1, (int) (ttlSeconds / 60));
        if (!properties.getMail().isEnabled()) {
            log.info("[DEV] Code de réinitialisation pour {} : {} (valide {} min)", to, code, minutes);
            return;
        }
        JavaMailSender sender = mailSenderProvider.getIfAvailable();
        if (sender == null) {
            log.warn("SMTP non configuré — code reset pour {} : {}", to, code);
            throw new BadRequestException("Envoi email impossible.");
        }
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(properties.getMail().getFrom());
        message.setTo(to);
        message.setSubject("Faso Tuuma — Réinitialisation de votre mot de passe");
        message.setText(
                "Bonjour,\n\n"
                        + "Votre code de réinitialisation Faso Tuuma est : "
                        + code
                        + "\nIl expire dans "
                        + minutes
                        + " minute(s). Ne le partagez avec personne.\n\n"
                        + "Si vous n'êtes pas à l'origine de cette demande, ignorez cet email.");
        try {
            sender.send(message);
        } catch (RuntimeException e) {
            log.error("Échec envoi email reset → {} : {}", to, e.getMessage());
            throw new BadRequestException("Impossible d'envoyer l'email. Réessayez plus tard.");
        }
    }
}
