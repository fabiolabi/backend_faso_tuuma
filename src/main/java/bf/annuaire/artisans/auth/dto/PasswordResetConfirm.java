package bf.annuaire.artisans.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Confirmation de réinitialisation : code reçu par email + nouveau mot de passe. */
public record PasswordResetConfirm(
        @NotBlank @Email String email,
        @NotBlank @Pattern(regexp = "\\d{6}", message = "Le code doit comporter 6 chiffres.") String code,
        @NotBlank @Size(min = 6, max = 100) String newPassword) {}
