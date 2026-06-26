package bf.annuaire.artisans.notification.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration de la feature {@code notification} (push FCM via Firebase Admin).
 *
 * <p>{@code enabled=false} (défaut dev/test) : aucun appel réseau, un émetteur de repli
 * {@code NoOpPushSender} journalise simplement. En prod, {@code enabled=true} + un fichier de
 * credentials Firebase (variable d'environnement {@code FIREBASE_CREDENTIALS_FILE}) activent l'envoi
 * réel. L'historique des notifications est persisté dans tous les cas.
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "app.notification")
public class NotificationProperties {

    /** Active l'envoi push réel via FCM. Faux ⇒ repli NoOp sans réseau. */
    private boolean enabled = false;

    private final Firebase firebase = new Firebase();

    @Getter
    @Setter
    public static class Firebase {
        /** Chemin du fichier JSON de compte de service Firebase. Jamais commité. */
        private String credentialsFile;

        /** Identifiant du projet Firebase (optionnel, déduit des credentials sinon). */
        private String projectId;
    }
}
