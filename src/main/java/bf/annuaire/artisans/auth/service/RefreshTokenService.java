package bf.annuaire.artisans.auth.service;

import bf.annuaire.artisans.auth.entity.RefreshToken;
import bf.annuaire.artisans.auth.repository.RefreshTokenRepository;
import bf.annuaire.artisans.auth.security.AuthProperties;
import bf.annuaire.artisans.auth.security.Tokens;
import bf.annuaire.artisans.common.exception.UnauthorizedException;
import bf.annuaire.artisans.user.entity.User;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Cycle de vie des refresh tokens persistés : émission, consommation (rotation) et révocation. Seul
 * le hash du token est stocké ; la valeur en clair n'existe que le temps de la réponse au mobile.
 */
@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final AuthProperties properties;

    /** Émet un nouveau refresh token pour l'utilisateur et le persiste (hashé). Renvoie la valeur claire. */
    @Transactional
    public String issue(User user) {
        Instant now = Instant.now();
        String rawToken = Tokens.randomRefreshToken();
        RefreshToken entity = new RefreshToken(
                user,
                Tokens.sha256Hex(rawToken),
                now.plusSeconds(properties.getJwt().getRefreshTokenTtlSeconds()),
                now);
        refreshTokenRepository.save(entity);
        return rawToken;
    }

    /**
     * Valide un refresh token et le révoque (rotation à usage unique). Renvoie l'utilisateur associé.
     *
     * @throws UnauthorizedException si le token est inconnu, expiré ou déjà révoqué.
     */
    @Transactional
    public User consume(String rawToken) {
        RefreshToken token = refreshTokenRepository
                .findByTokenHash(Tokens.sha256Hex(rawToken))
                .orElseThrow(() -> new UnauthorizedException("Refresh token invalide."));
        if (!token.isActive(Instant.now())) {
            throw new UnauthorizedException("Refresh token expiré ou révoqué.");
        }
        token.setRevokedAt(Instant.now());
        return token.getUser();
    }

    /** Révoque le refresh token fourni s'il existe et est encore actif (logout). */
    @Transactional
    public void revoke(String rawToken) {
        refreshTokenRepository
                .findByTokenHash(Tokens.sha256Hex(rawToken))
                .filter(t -> t.getRevokedAt() == null)
                .ifPresent(t -> t.setRevokedAt(Instant.now()));
    }

    /** Révoque tous les refresh tokens actifs d'un utilisateur (ex. après reset de mot de passe). */
    @Transactional
    public void revokeAllForUser(Long userId) {
        refreshTokenRepository.revokeAllActiveForUser(userId, Instant.now());
    }
}
