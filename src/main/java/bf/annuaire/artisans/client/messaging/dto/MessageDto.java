package bf.annuaire.artisans.client.messaging.dto;

import java.time.Instant;

/** Vue d'un message. */
public record MessageDto(
        Long id,
        Long conversationId,
        Long senderUserId,
        String body,
        Instant sentAt,
        Instant readAt) {}
