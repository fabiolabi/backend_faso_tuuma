package bf.annuaire.artisans.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Demande de réinitialisation : envoie un code OTP à 6 chiffres par SMS sur le téléphone du compte. */
public record PasswordResetRequest(@NotBlank @Size(max = 30) String phone) {}
