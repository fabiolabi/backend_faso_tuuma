package bf.annuaire.artisans.common.config;

import org.springframework.context.annotation.Configuration;

/**
 * Squelette de configuration WebSocket.
 *
 * <p>Prévu pour des fonctionnalités temps-réel futures (ex. messagerie {@code Conversation}/
 * {@code Message}). Non utilisé dans le socle initial : l'activation (broker, endpoints STOMP)
 * sera ajoutée quand la feature sera implémentée.
 */
@Configuration
public class WebSocketConfig {
    // TODO: activer @EnableWebSocketMessageBroker et configurer le broker/endpoints
    //       lors de l'implémentation de la messagerie temps-réel.
}
