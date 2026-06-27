package bf.annuaire.artisans.auth.service;

import bf.annuaire.artisans.auth.dto.AuthResponse;
import bf.annuaire.artisans.auth.dto.LoginRequest;
import bf.annuaire.artisans.auth.dto.RegisterRequest;
import bf.annuaire.artisans.auth.security.JwtService;
import bf.annuaire.artisans.common.exception.BadRequestException;
import bf.annuaire.artisans.common.exception.UnauthorizedException;
import bf.annuaire.artisans.user.entity.RoleName;
import bf.annuaire.artisans.user.entity.User;
import bf.annuaire.artisans.user.mapper.UserMapper;
import bf.annuaire.artisans.user.service.UserService;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Orchestration des flux d'authentification : inscription, connexion, rafraîchissement et
 * déconnexion. Émet à chaque succès une paire access token (JWT) + refresh token persisté.
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final UserMapper userMapper;

    /** Inscrit un nouvel utilisateur (rôle CLIENT ou ARTISAN) et le connecte immédiatement. */
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        RoleName role = request.role() == null ? RoleName.CLIENT : request.role();
        if (role == RoleName.ADMIN) {
            throw new BadRequestException("Le rôle ADMIN ne peut pas être attribué à l'inscription.");
        }
        User user = userService.createUser(
                request.firstname(),
                request.lastname(),
                request.phone(),
                request.email(),
                request.password(),
                role);
        return buildAuthResponse(user);
    }

    /** Authentifie par téléphone + mot de passe et émet une paire de tokens. */
    @Transactional
    public AuthResponse login(LoginRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.phone(), request.password()));
        } catch (AuthenticationException ex) {
            throw new UnauthorizedException("Identifiants invalides.");
        }
        User user = userService.getByPhone(request.phone());
        user.getCredential().setLastLogin(Instant.now());
        return buildAuthResponse(user);
    }

    /** Échange un refresh token valide contre une nouvelle paire (rotation à usage unique). */
    @Transactional
    public AuthResponse refresh(String rawRefreshToken) {
        User user = refreshTokenService.consume(rawRefreshToken);
        User full = userService.getByPhone(user.getPhone());
        return buildAuthResponse(full);
    }

    /** Révoque le refresh token fourni (déconnexion). */
    @Transactional
    public void logout(String rawRefreshToken) {
        refreshTokenService.revoke(rawRefreshToken);
    }

    /**
     * Active le rôle ARTISAN pour un client existant et émet de nouveaux tokens (rôles à jour dans le
     * JWT).
     */
    @Transactional
    public AuthResponse becomeArtisan(Long userId) {
        User user = userService.grantRole(userId, RoleName.ARTISAN);
        return buildAuthResponse(user);
    }

    private AuthResponse buildAuthResponse(User user) {
        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = refreshTokenService.issue(user);
        return AuthResponse.of(
                accessToken, refreshToken, jwtService.getAccessTokenTtlSeconds(), userMapper.toDto(user));
    }
}
