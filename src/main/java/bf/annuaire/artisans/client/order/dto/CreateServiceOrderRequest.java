package bf.annuaire.artisans.client.order.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Instant;

/**
 * Création d'une demande de prestation. {@code serviceId} est optionnel (demande libre si absent) ;
 * {@code requestedDate} est la date souhaitée par le client (facultative).
 */
public record CreateServiceOrderRequest(
        @NotNull Long metierId,
        Long serviceId,
        @Size(max = 2000) String message,
        Instant requestedDate) {}
