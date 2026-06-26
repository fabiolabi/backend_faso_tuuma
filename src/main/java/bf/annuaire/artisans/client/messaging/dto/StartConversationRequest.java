package bf.annuaire.artisans.client.messaging.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Démarre (ou récupère) un fil de discussion avec une enseigne. {@code firstMessage} est optionnel :
 * s'il est présent, il est envoyé comme premier message du fil.
 */
public record StartConversationRequest(
        @NotNull Long metierId,
        @Size(max = 4000) String firstMessage) {}
