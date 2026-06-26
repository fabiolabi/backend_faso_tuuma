package bf.annuaire.artisans.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/** Demande de réinitialisation : déclenche l'envoi d'un code à 6 chiffres sur l'email du compte. */
public record PasswordResetRequest(@NotBlank @Email String email) {}
