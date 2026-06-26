package bf.annuaire.artisans.client.messaging.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Envoi d'un message dans un fil existant. */
public record SendMessageRequest(
        @NotBlank @Size(max = 4000) String body) {}
