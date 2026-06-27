package bf.annuaire.artisans.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Confirmation de réinitialisation : code OTP reçu par SMS + nouveau mot de passe. */
public record PasswordResetConfirm(
        @NotBlank @Size(max = 30) String phone,
        @NotBlank @Pattern(regexp = "\\d{6}", message = "Le code doit comporter 6 chiffres.") String code,
        @NotBlank @Size(min = 6, max = 100) String newPassword) {}
