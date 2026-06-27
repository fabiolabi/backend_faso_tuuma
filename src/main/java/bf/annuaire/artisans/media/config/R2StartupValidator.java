package bf.annuaire.artisans.media.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;

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
        String bucket = mediaProperties.getR2().getBucket();
        s3Client.headBucket(HeadBucketRequest.builder().bucket(bucket).build());
        log.info(
                "Cloudflare R2 connecté — bucket={}, publicUrl={}",
                bucket,
                StringUtils.hasText(mediaProperties.getPublicBaseUrl())
                        ? mediaProperties.getPublicBaseUrl()
                        : "(proxy /api/media/{id})");
    }
}
