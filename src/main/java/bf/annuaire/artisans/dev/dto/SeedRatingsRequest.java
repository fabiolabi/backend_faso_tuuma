package bf.annuaire.artisans.dev.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

/** Paramètres du seed d'avis sur les commerces déjà publiés (répartition aléatoire). */
public record SeedRatingsRequest(
        /** Nombre total d'avis à créer (commerces tirés au hasard). */
        @Min(1) @Max(500) Integer totalReviews,
        /** Plafond d'avis par commerce pour éviter qu'un seul absorbe tout. */
        @Min(1) @Max(20) Integer maxReviewsPerMetier,
        /** Alias de maxReviewsPerMetier (rétrocompatibilité). */
        @Min(1) @Max(20) Integer reviewsPerMetier,
        @Min(1) @Max(500) Integer userCount,
        @Size(min = 6, max = 100) String password,
        @Size(max = 12) String phonePrefix) {}
