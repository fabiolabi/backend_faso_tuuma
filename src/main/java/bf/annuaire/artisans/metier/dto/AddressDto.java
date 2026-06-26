package bf.annuaire.artisans.metier.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Adresse d'une enseigne (entrée et sortie). {@code city} obligatoire dès qu'une adresse est fournie.
 */
public record AddressDto(
        @NotBlank @Size(max = 255) String city,
        @Size(max = 255) String district,
        @Size(max = 255) String sector,
        @Size(max = 255) String street) {}
