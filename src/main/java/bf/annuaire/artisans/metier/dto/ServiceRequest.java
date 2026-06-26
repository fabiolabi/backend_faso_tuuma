package bf.annuaire.artisans.metier.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

/** Création / mise à jour d'une prestation. {@code active} défaut {@code true} si absent. */
public record ServiceRequest(
        @NotBlank @Size(max = 255) String name,
        String description,
        @PositiveOrZero Long priceMin,
        @PositiveOrZero Long priceMax,
        Boolean active) {}
