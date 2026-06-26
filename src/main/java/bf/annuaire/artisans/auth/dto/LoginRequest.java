package bf.annuaire.artisans.auth.dto;

import jakarta.validation.constraints.NotBlank;

/** Connexion par téléphone + mot de passe. */
public record LoginRequest(@NotBlank String phone, @NotBlank String password) {}
