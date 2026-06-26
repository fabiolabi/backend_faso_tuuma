package bf.annuaire.artisans.auth.dto;

import jakarta.validation.constraints.NotBlank;

/** Déconnexion : révoque le refresh token fourni. */
public record LogoutRequest(@NotBlank String refreshToken) {}
