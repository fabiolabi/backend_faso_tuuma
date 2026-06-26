package bf.annuaire.artisans.auth.dto;

import bf.annuaire.artisans.user.entity.RoleName;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Inscription d'un nouvel utilisateur. Le {@code role} est optionnel (défaut {@code CLIENT}) et ne
 * peut valoir que {@code CLIENT} ou {@code ARTISAN}. L'{@code email} est optionnel mais requis pour
 * pouvoir réinitialiser son mot de passe.
 */
public record RegisterRequest(
        @NotBlank @Size(max = 255) String firstname,
        @NotBlank @Size(max = 255) String lastname,
        @NotBlank @Size(max = 30) String phone,
        @Email @Size(max = 255) String email,
        @NotBlank @Size(min = 6, max = 100) String password,
        RoleName role) {}
