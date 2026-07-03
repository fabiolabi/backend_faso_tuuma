package bf.annuaire.artisans.dev.dto;

import java.util.List;

/** Résultat du seed d'avis. */
public record SeedRatingsResponse(
        int usersCreated,
        int reviewsCreated,
        int metiersUpdated,
        List<Long> metierIds) {}
