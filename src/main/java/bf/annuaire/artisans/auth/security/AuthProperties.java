package bf.annuaire.artisans.auth.security;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Propriétés de configuration de l'authentification, liées au préfixe {@code app}
 * (cf. {@code application.properties}). Aucune valeur sensible en dur : le secret JWT provient d'une
 * variable d'environnement.
 */
@Component
@ConfigurationProperties(prefix = "app")
@Getter
@Setter
public class AuthProperties {

    private final Jwt jwt = new Jwt();
    private final PasswordReset passwordReset = new PasswordReset();
    private final Mail mail = new Mail();

    @Getter
    @Setter
    public static class Jwt {
        /** Secret HMAC (>= 32 octets) du token d'accès. */
        private String secret;
        /** Durée de vie de l'access token, en secondes. */
        private long accessTokenTtlSeconds = 900;
        /** Durée de vie du refresh token, en secondes. */
        private long refreshTokenTtlSeconds = 2_592_000;
    }

    @Getter
    @Setter
    public static class PasswordReset {
        /** Durée de validité du code à 6 chiffres, en secondes. */
        private long codeTtlSeconds = 900;
    }

    @Getter
    @Setter
    public static class Mail {
        /** Adresse d'expédition des emails. */
        private String from = "no-reply@fasotuuma.bf";
        /** Si false (dev), le code de reset est écrit dans les logs au lieu d'être envoyé. */
        private boolean enabled = true;
    }
}
