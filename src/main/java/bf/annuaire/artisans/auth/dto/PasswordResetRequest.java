package bf.annuaire.artisans.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

/**
 * Demande de réinitialisation : envoie un code OTP à 6 chiffres par SMS (téléphone) ou par email.
 *
 * <p>Exactement un des deux identifiants doit être renseigné.
 */
public record PasswordResetRequest(
        @Size(max = 30) String phone, @Email @Size(max = 255) String email) {}
