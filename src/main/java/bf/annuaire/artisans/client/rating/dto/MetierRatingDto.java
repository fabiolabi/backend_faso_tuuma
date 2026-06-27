package bf.annuaire.artisans.client.rating.dto;

import java.time.Instant;

/** Vue d'un avis sur une enseigne. */
public record MetierRatingDto(
        Long id,
        Long metierId,
        String metierName,
        Long clientUserId,
        String clientName,
        short starRating,
        String comment,
        Short aiRating,
        String sentimentLabel,
        boolean mismatch,
        String status,
        Instant createdAt) {}
