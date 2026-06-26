package bf.annuaire.artisans.auth.service;

import bf.annuaire.artisans.auth.security.AuthProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * Envoi des emails transactionnels (code de réinitialisation de mot de passe).
 *
 * <p>En dev (aucun serveur SMTP, {@code app.mail.enabled=false}), le code est écrit dans les logs
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
    public void sendPasswordResetCode(String to, String code) {
        JavaMailSender sender = mailSenderProvider.getIfAvailable();
        if (!properties.getMail().isEnabled() || sender == null) {
            log.info("[DEV] Code de réinitialisation pour {} : {}", to, code);
            return;
        }
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(properties.getMail().getFrom());
        message.setTo(to);
        message.setSubject("Faso Tuuma — Réinitialisation de votre mot de passe");
        message.setText(
                "Votre code de réinitialisation est : " + code
                        + "\nIl expire dans quelques minutes. Si vous n'êtes pas à l'origine de cette demande,"
                        + " ignorez cet email.");
        sender.send(message);
    }
}
