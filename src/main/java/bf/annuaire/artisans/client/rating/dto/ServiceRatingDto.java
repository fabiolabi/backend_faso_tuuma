package bf.annuaire.artisans.client.rating.dto;

import java.time.Instant;

/**
 * Vue d'un avis sur une prestation. {@code serviceName} est utile à la vue « mes avis ». Les champs
 * IA ({@code aiRating}, {@code sentimentLabel}, {@code mismatch}, {@code status}) sont en lecture
 * seule ; les détails internes de modération (faux avis, motif) ne sont jamais exposés.
 */
public record ServiceRatingDto(
        Long id,
        Long serviceId,
        String serviceName,
        Long clientUserId,
        String clientName,
        short starRating,
        String comment,
        Short aiRating,
        String sentimentLabel,
        boolean mismatch,
        String status,
        Instant createdAt) {}
