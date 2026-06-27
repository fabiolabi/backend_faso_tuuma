package bf.annuaire.artisans.media.config;

import bf.annuaire.artisans.media.config.MediaProperties.R2Properties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/** Log de la configuration media au démarrage (sans secrets). */
@Component
@RequiredArgsConstructor
@Slf4j
public class MediaStartupLogger implements ApplicationRunner {

    private final MediaProperties mediaProperties;

    @Override
    public void run(ApplicationArguments args) {
        log.info(
                "Media — backend={}, maxSize={} octets, publicUrl={}",
                mediaProperties.getBackend(),
                mediaProperties.getMaxFileSizeBytes(),
                StringUtils.hasText(mediaProperties.getPublicBaseUrl())
                        ? mediaProperties.getPublicBaseUrl()
                        : "(proxy /api/media/{id})");

        if ("local".equalsIgnoreCase(mediaProperties.getBackend())) {
            log.info("Media local — storageDir={}", mediaProperties.getStorageDir());
            return;
        }

        if ("r2".equalsIgnoreCase(mediaProperties.getBackend())) {
            R2Properties r2 = mediaProperties.getR2();
            log.info(
                    "Media R2 — bucket={}, endpoint={}, region={}, autoCreateBucket={}, migrateLocal={}",
                    mask(r2.getBucket()),
                    r2.getEndpoint(),
                    r2.getRegion(),
                    r2.isAutoCreateBucket(),
                    mediaProperties.isMigrateLocalToR2());
            log.info(
                    "Media R2 — accessKeyId={} (secret présent={})",
                    mask(r2.getAccessKeyId()),
                    StringUtils.hasText(r2.getSecretAccessKey()));
        }
    }

    private static String mask(String value) {
        if (!StringUtils.hasText(value)) {
            return "(vide)";
        }
        if (value.length() <= 4) {
            return "****";
        }
        return value.substring(0, 2) + "****" + value.substring(value.length() - 2);
    }
}
