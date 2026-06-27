package bf.annuaire.artisans.media.config;

import bf.annuaire.artisans.media.repository.MediaFileRepository;
import bf.annuaire.artisans.media.service.R2MediaStorage;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Migration one-shot : copie les fichiers du disque local ({@code storageDir}) vers R2.
 *
 * <p>Activer avec {@code MEDIA_MIGRATE_LOCAL_TO_R2=true} une seule fois, puis repasser à {@code false}.
 */
@Component
@ConditionalOnProperty(prefix = "app.media", name = "backend", havingValue = "r2")
@ConditionalOnProperty(prefix = "app.media", name = "migrate-local-to-r2", havingValue = "true")
@RequiredArgsConstructor
@Slf4j
public class LocalToR2MediaMigration implements ApplicationRunner {

    private final MediaFileRepository mediaFileRepository;
    private final R2MediaStorage r2MediaStorage;
    private final MediaProperties mediaProperties;

    @Override
    public void run(ApplicationArguments args) {
        Path root = Paths.get(mediaProperties.getStorageDir()).toAbsolutePath().normalize();
        log.info("Migration media local → R2 depuis {}", root);

        int uploaded = 0;
        int skipped = 0;
        int missing = 0;

        for (var mediaFile : mediaFileRepository.findAll()) {
            String key = mediaFile.getStoredPath();
            if (r2MediaStorage.exists(key)) {
                skipped++;
                continue;
            }

            Path localFile = root.resolve(key).normalize();
            if (!localFile.startsWith(root) || !Files.isReadable(localFile)) {
                log.warn("Fichier local absent pour media id={} path={}", mediaFile.getId(), key);
                missing++;
                continue;
            }

            r2MediaStorage.uploadFromPath(
                    key, localFile, mediaFile.getContentType(), mediaFile.getSizeBytes());
            uploaded++;
        }

        log.info(
                "Migration R2 terminée — uploadés={}, déjà présents={}, absents={}",
                uploaded,
                skipped,
                missing);
    }
}
