package bf.annuaire.artisans.auth.dto;

import bf.annuaire.artisans.user.dto.UserDto;

/**
 * Réponse d'authentification renvoyée au mobile : paire de tokens + profil. {@code expiresIn} est la
 * durée de vie de l'access token en secondes ; le {@code refreshToken} sert à obtenir une nouvelle
 * paire sans ressaisir le mot de passe.
 */
public record AuthResponse(
        String accessToken, String refreshToken, String tokenType, long expiresIn, UserDto user) {

    public static AuthResponse of(String accessToken, String refreshToken, long expiresIn, UserDto user) {
        return new AuthResponse(accessToken, refreshToken, "Bearer", expiresIn, user);
    }
}
