package bf.annuaire.artisans.client.rating.dto;

import java.time.Instant;

/** Vue d'une note d'enseigne. {@code metierName} est utile à la vue « mes notes ». */
public record MetierRatingDto(
        Long id,
        Long metierId,
        String metierName,
        Long clientUserId,
        String clientName,
        short rating,
        String comment,
        Instant createdAt) {}
