package bf.annuaire.artisans.client.messaging.dto;

import java.time.Instant;

/**
 * Vue d'un fil de discussion. {@code lastMessagePreview} et {@code unreadCount} sont calculés du
 * point de vue du lecteur courant (le compteur de non-lus ignore ses propres messages).
 */
public record ConversationDto(
        Long id,
        Long metierId,
        String metierName,
        Long clientUserId,
        String clientName,
        Instant lastMessageAt,
        String lastMessagePreview,
        long unreadCount,
        Instant createdAt) {}
