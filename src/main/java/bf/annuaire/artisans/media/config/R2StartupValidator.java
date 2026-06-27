package bf.annuaire.artisans.media.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;
import software.amazon.awssdk.services.s3.model.NoSuchBucketException;
import software.amazon.awssdk.services.s3.model.S3Exception;

/** Vérifie la connexion R2 au démarrage (échec rapide si mal configuré). */
@Component
@ConditionalOnProperty(prefix = "app.media", name = "backend", havingValue = "r2")
@RequiredArgsConstructor
@Slf4j
public class R2StartupValidator implements ApplicationRunner {

    private final S3Client s3Client;
    private final MediaProperties mediaProperties;

    @Override
    public void run(ApplicationArguments args) {
        var r2 = mediaProperties.getR2();
        String bucket = r2.getBucket();
        String endpoint = r2.getEndpoint();

        if (!StringUtils.hasText(bucket)) {
            throw new IllegalStateException(
                    "R2_BUCKET est vide dans l'environnement. Vérifiez le fichier .env puis "
                            + "redémarrez avec : docker compose up -d --force-recreate app");
        }
        if (!StringUtils.hasText(endpoint)) {
            throw new IllegalStateException(
                    "R2_ENDPOINT est vide. Format attendu : https://<account_id>.r2.cloudflarestorage.com");
        }

        try {
            s3Client.headBucket(HeadBucketRequest.builder().bucket(bucket).build());
        } catch (NoSuchBucketException e) {
            if (!r2.isAutoCreateBucket()) {
                throw new IllegalStateException(
                        "Bucket R2 '" + bucket + "' introuvable sur " + endpoint + ". "
                                + "Créez-le dans le dashboard Cloudflare (R2 → Create bucket) "
                                + "ou activez R2_AUTO_CREATE_BUCKET=true dans .env",
                        e);
            }
            log.warn("Bucket R2 '{}' absent — création automatique...", bucket);
            s3Client.createBucket(CreateBucketRequest.builder().bucket(bucket).build());
            log.info("Bucket R2 '{}' créé.", bucket);
        } catch (S3Exception e) {
            log.error(
                    "Connexion R2 échouée — bucket={}, endpoint={}, awsError={}",
                    bucket,
                    endpoint,
                    e.awsErrorDetails(),
                    e);
            throw new IllegalStateException(
                    "Connexion R2 échouée (bucket=" + bucket + ", endpoint=" + endpoint + "). "
                            + "Vérifiez R2_BUCKET, R2_ENDPOINT et les clés API.",
                    e);
        }

        log.info(
                "Cloudflare R2 connecté — bucket={}, endpoint={}, publicUrl={}",
                bucket,
                endpoint,
                StringUtils.hasText(mediaProperties.getPublicBaseUrl())
                        ? mediaProperties.getPublicBaseUrl()
                        : "(proxy /api/media/{id})");
    }
}
