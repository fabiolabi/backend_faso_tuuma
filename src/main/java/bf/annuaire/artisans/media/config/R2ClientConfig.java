package bf.annuaire.artisans.media.config;

import java.net.URI;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;

/**
 * Client S3 compatible Cloudflare R2 (activé lorsque {@code app.media.backend=r2}).
 */
@Configuration
@ConditionalOnProperty(prefix = "app.media", name = "backend", havingValue = "r2")
public class R2ClientConfig {

    @Bean(destroyMethod = "close")
    S3Client r2S3Client(MediaProperties properties) {
        validateR2Config(properties);
        return S3Client.builder()
                .endpointOverride(URI.create(properties.getR2().getEndpoint()))
                .region(Region.of(properties.getR2().getRegion()))
                .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(
                        properties.getR2().getAccessKeyId(), properties.getR2().getSecretAccessKey())))
                .serviceConfiguration(S3Configuration.builder()
                        .pathStyleAccessEnabled(true)
                        .build())
                .build();
    }

    private static void validateR2Config(MediaProperties properties) {
        var r2 = properties.getR2();
        if (!StringUtils.hasText(r2.getBucket())) {
            throw new IllegalStateException("app.media.r2.bucket est requis lorsque app.media.backend=r2");
        }
        if (!StringUtils.hasText(r2.getEndpoint())) {
            throw new IllegalStateException("app.media.r2.endpoint est requis lorsque app.media.backend=r2");
        }
        if (!StringUtils.hasText(r2.getAccessKeyId()) || !StringUtils.hasText(r2.getSecretAccessKey())) {
            throw new IllegalStateException(
                    "app.media.r2.access-key-id et app.media.r2.secret-access-key sont requis lorsque app.media.backend=r2");
        }
    }
}
