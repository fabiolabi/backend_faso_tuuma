package bf.annuaire.artisans.metier.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Numéro de contact d'une enseigne (entrée et sortie). {@code whatsapp} = joignable sur WhatsApp. */
public record MetierPhoneDto(
        @NotBlank @Size(max = 30) String number, boolean whatsapp, @Size(max = 60) String label) {}
