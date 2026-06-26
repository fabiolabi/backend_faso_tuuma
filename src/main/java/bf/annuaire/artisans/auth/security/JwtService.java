package bf.annuaire.artisans.auth.security;

import bf.annuaire.artisans.user.entity.Role;
import bf.annuaire.artisans.user.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.crypto.SecretKey;
import org.springframework.stereotype.Service;

/**
 * Génération et validation de l'access token JWT (HMAC-SHA256, JJWT). Token stateless transmis par le
 * header {@code Authorization: Bearer <token>} ; les claims portent l'id, le téléphone et les rôles
 * de l'utilisateur — aucune lecture en base n'est nécessaire pour authentifier une requête.
 */
@Service
public class JwtService {

    private static final String CLAIM_PHONE = "phone";
    private static final String CLAIM_ROLES = "roles";

    private final SecretKey key;
    private final long accessTokenTtlSeconds;

    public JwtService(AuthProperties properties) {
        this.key = Keys.hmacShaKeyFor(properties.getJwt().getSecret().getBytes(StandardCharsets.UTF_8));
        this.accessTokenTtlSeconds = properties.getJwt().getAccessTokenTtlSeconds();
    }

    /** Émet un access token signé pour l'utilisateur (sujet = id, claims phone + rôles). */
    public String generateAccessToken(User user) {
        Instant now = Instant.now();
        List<String> roles =
                user.getRoles().stream().map(Role::getName).map(Enum::name).collect(Collectors.toList());
        return Jwts.builder()
                .subject(String.valueOf(user.getId()))
                .claim(CLAIM_PHONE, user.getPhone())
                .claim(CLAIM_ROLES, roles)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(accessTokenTtlSeconds)))
                .signWith(key)
                .compact();
    }

    /**
     * Valide la signature et l'expiration du token et en extrait l'identité.
     *
     * @throws JwtException si le token est invalide ou expiré.
     */
    public AuthPrincipal parse(String token) {
        Claims claims = Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
        Long userId = Long.valueOf(claims.getSubject());
        String phone = claims.get(CLAIM_PHONE, String.class);
        @SuppressWarnings("unchecked")
        List<String> roles = claims.get(CLAIM_ROLES, List.class);
        return new AuthPrincipal(userId, phone, roles == null ? Set.of() : Set.copyOf(roles));
    }

    public long getAccessTokenTtlSeconds() {
        return accessTokenTtlSeconds;
    }
}
