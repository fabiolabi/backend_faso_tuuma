package bf.annuaire.artisans.notification.sender;

import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;

/**
 * Émetteur de repli sans réseau, utilisé quand {@code app.notification.enabled=false} (dev/test) ou
 * qu'aucun credential Firebase n'est configuré. Journalise l'envoi et ne rejette aucun token.
 */
@Slf4j
public class NoOpPushSender implements PushSender {

    @Override
    public List<String> send(List<String> tokens, String title, String body, Map<String, String> data) {
        log.debug("[push:noop] {} token(s), titre='{}'", tokens == null ? 0 : tokens.size(), title);
        return List.of();
    }
}
