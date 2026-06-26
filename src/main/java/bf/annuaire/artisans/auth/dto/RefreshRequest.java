package bf.annuaire.artisans.auth.dto;

import jakarta.validation.constraints.NotBlank;

/** Demande de rafraîchissement : échange un refresh token valide contre une nouvelle paire. */
public record RefreshRequest(@NotBlank String refreshToken) {}
