package bf.annuaire.artisans.client.rating.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** Dépôt ou mise à jour d'une note (1 à 5) avec commentaire optionnel. */
public record RatingRequest(
        @NotNull @Min(1) @Max(5) Integer rating,
        @Size(max = 2000) String comment) {}
