package bf.annuaire.artisans.metier.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Set;

/**
 * Vue légère d'une enseigne pour les résultats de recherche (jamais l'entité exposée).
 * {@code distanceKm} n'est renseigné que si la recherche fournit une position GPS.
 */
public record MetierSummaryDto(
        Long id,
        String name,
        String phone,
        String coverUrl,
        String city,
        String district,
        BigDecimal gpsLat,
        BigDecimal gpsLng,
        BigDecimal ratingAvg,
        Integer ratingCount,
        Set<String> categories,
        Double distanceKm,
        Instant createdAt,
        Instant updatedAt) {}
