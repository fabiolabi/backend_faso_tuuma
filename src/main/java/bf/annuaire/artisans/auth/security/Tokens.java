package bf.annuaire.artisans.auth.security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HexFormat;

/**
 * Fabrique de secrets opaques (refresh tokens, codes de reset) et hachage SHA-256.
 *
 * <p>Les valeurs envoyées au client sont aléatoires ; seul leur hash est persisté en base, jamais la
 * valeur en clair.
 */
public final class Tokens {

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final Base64.Encoder URL_ENCODER = Base64.getUrlEncoder().withoutPadding();

    private Tokens() {}

    /** Génère un refresh token opaque (256 bits) encodé en base64url. */
    public static String randomRefreshToken() {
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        return URL_ENCODER.encodeToString(bytes);
    }

    /** Génère un code numérique à 6 chiffres (000000–999999). */
    public static String sixDigitCode() {
        return String.format("%06d", RANDOM.nextInt(1_000_000));
    }

    /** Hash SHA-256 (hex) d'une valeur, pour stockage en base. */
    public static String sha256Hex(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 indisponible", e);
        }
    }
}
