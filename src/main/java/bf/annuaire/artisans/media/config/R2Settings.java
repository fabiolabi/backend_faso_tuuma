package bf.annuaire.artisans.media.config;

import java.net.URI;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

/** Normalise la configuration R2 (erreurs fréquentes dans le .env). */
@Slf4j
final class R2Settings {

    private R2Settings() {}

    /**
     * L'endpoint R2 est au niveau du compte, sans nom de bucket dans l'URL.
     * Ex. {@code https://<account_id>.r2.cloudflarestorage.com} — PAS {@code .../mon-bucket}.
     */
    static void normalize(MediaProperties properties) {
        var r2 = properties.getR2();
        if (r2 == null) {
            return;
        }

        String endpoint = r2.getEndpoint();
        if (!StringUtils.hasText(endpoint)) {
            return;
        }

        String original = endpoint.trim();
        String normalized = stripBucketPathFromEndpoint(original, r2.getBucket());

        if (!original.equals(normalized)) {
            log.warn(
                    "R2_ENDPOINT corrigé automatiquement : '{}' → '{}'. "
                            + "Le bucket va dans R2_BUCKET, pas dans l'URL.",
                    original,
                    normalized);
            r2.setEndpoint(normalized);
        }
    }

    static String stripBucketPathFromEndpoint(String endpoint, String bucket) {
        String value = endpoint.trim();
        while (value.endsWith("/")) {
            value = value.substring(0, value.length() - 1);
        }

        if (StringUtils.hasText(bucket) && value.endsWith("/" + bucket)) {
            value = value.substring(0, value.length() - bucket.length() - 1);
        }

        try {
            URI uri = URI.create(value);
            String path = uri.getPath();
            if (path != null && !path.isBlank() && !"/".equals(path)) {
                value = uri.getScheme() + "://" + uri.getHost();
            }
        } catch (IllegalArgumentException ignored) {
            // Garde la valeur telle quelle ; l'erreur remontera au client S3.
        }

        return value;
    }
}
