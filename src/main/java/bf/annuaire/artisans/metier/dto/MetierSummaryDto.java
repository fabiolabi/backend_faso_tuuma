package bf.annuaire.artisans.metier.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Set;

/**
 * Vue légère d'une enseigne pour les résultats de recherche (jamais l'entité exposée).
 * {@code distanceKm} n'est renseigné que si la recherche fournit une position GPS. La notation est
 * portée par les services : pas de note d'enseigne ici.
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
        Set<String> categories,
        Double distanceKm,
        Instant createdAt,
        Instant updatedAt) {}
