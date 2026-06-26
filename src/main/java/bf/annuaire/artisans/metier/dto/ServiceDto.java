package bf.annuaire.artisans.metier.dto;

import java.math.BigDecimal;

/**
 * Prestation d'une enseigne (sortie). Prix en FCFA, optionnels. {@code ratingAvg} / {@code ratingCount}
 * sont la note calculée par l'IA (lecture seule) ; {@code aiSummary} la synthèse d'avis générée.
 */
public record ServiceDto(
        Long id,
        String name,
        String description,
        Long priceMin,
        Long priceMax,
        boolean active,
        BigDecimal ratingAvg,
        Integer ratingCount,
        String aiSummary) {}
