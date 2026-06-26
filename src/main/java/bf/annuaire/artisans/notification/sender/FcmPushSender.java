package bf.annuaire.artisans.notification.sender;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.MessagingErrorCode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Émetteur push réel via Firebase Cloud Messaging. Envoie un message par token et collecte ceux que
 * FCM rejette comme invalides ({@code UNREGISTERED} / {@code INVALID_ARGUMENT}) afin que l'appelant
 * les purge. Instancié par {@code FirebaseConfig} uniquement quand un bean {@link FirebaseMessaging}
 * est disponible.
 */
@Slf4j
@RequiredArgsConstructor
public class FcmPushSender implements PushSender {

    private final FirebaseMessaging messaging;

    @Override
    public List<String> send(List<String> tokens, String title, String body, Map<String, String> data) {
        List<String> invalid = new ArrayList<>();
        for (String token : tokens) {
            Message message = Message.builder()
                    .setToken(token)
                    .setNotification(com.google.firebase.messaging.Notification.builder()
                            .setTitle(title)
                            .setBody(body)
                            .build())
                    .putAllData(data == null ? Map.of() : data)
                    .build();
            try {
                messaging.send(message);
            } catch (FirebaseMessagingException e) {
                MessagingErrorCode code = e.getMessagingErrorCode();
                if (code == MessagingErrorCode.UNREGISTERED || code == MessagingErrorCode.INVALID_ARGUMENT) {
                    invalid.add(token);
                }
                log.warn("Envoi FCM échoué (code={}) : {}", code, e.getMessage());
            }
        }
        return invalid;
    }
}
