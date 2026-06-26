package bf.annuaire.artisans.notification.sender;

import java.util.List;
import java.util.Map;

/**
 * Abstraction d'envoi de notifications push. Deux implémentations : {@code FcmPushSender} (réel, via
 * Firebase) et {@code NoOpPushSender} (repli sans réseau quand {@code app.notification.enabled=false}).
 */
public interface PushSender {

    /**
     * Envoie une notification aux tokens fournis.
     *
     * @return les tokens rejetés par le service (invalides / désinscrits), à purger côté appelant.
     */
    List<String> send(List<String> tokens, String title, String body, Map<String, String> data);
}
