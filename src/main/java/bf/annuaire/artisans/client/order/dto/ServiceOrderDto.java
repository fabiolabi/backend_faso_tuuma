package bf.annuaire.artisans.client.order.dto;

import bf.annuaire.artisans.client.order.entity.ServiceOrderStatus;
import java.time.Instant;

/**
 * Vue d'une demande de prestation. {@code clientName} n'est utile qu'à la vue artisan ;
 * {@code serviceId} / {@code serviceName} sont {@code null} pour une demande libre.
 */
public record ServiceOrderDto(
        Long id,
        Long metierId,
        String metierName,
        Long serviceId,
        String serviceName,
        Long clientUserId,
        String clientName,
        String message,
        Instant requestedDate,
        ServiceOrderStatus status,
        Instant createdAt,
        Instant updatedAt) {}
