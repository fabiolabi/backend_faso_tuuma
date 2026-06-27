package bf.annuaire.artisans.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Mise à jour du profil utilisateur (mobile). */
public record UpdateProfileRequest(
        @NotBlank @Size(max = 255) String firstname,
        @NotBlank @Size(max = 255) String lastname,
        @Size(max = 255) String email,
        @Size(max = 255) String city) {}
