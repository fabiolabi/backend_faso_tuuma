package bf.annuaire.artisans.media.service;

import bf.annuaire.artisans.media.config.MediaProperties;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.PathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

/**
 * Stockage des fichiers sur le système de fichiers local (développement / repli).
 *
 * <p>Le chemin relatif est entièrement généré côté serveur ({@code yyyy/MM/uuid.ext}) : il n'est
 * jamais dérivé du nom fourni par le client, ce qui élimine tout risque de path traversal.
 */
@Component
@ConditionalOnProperty(prefix = "app.media", name = "backend", havingValue = "local", matchIfMissing = true)
public class LocalMediaStorage implements MediaStorageBackend {

    private static final DateTimeFormatter SHARD =
            DateTimeFormatter.ofPattern("yyyy/MM").withZone(ZoneOffset.UTC);

    private final Path root;

    public LocalMediaStorage(MediaProperties properties) {
        this.root = Paths.get(properties.getStorageDir()).toAbsolutePath().normalize();
    }

    @Override
    public String store(InputStream content, String extension, String contentType, long sizeBytes) {
        String relativePath = SHARD.format(Instant.now()) + "/" + UUID.randomUUID() + extension;
        Path target = resolve(relativePath);
        try {
            Files.createDirectories(target.getParent());
            Files.copy(content, target, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new MediaStorageException("Échec de l'écriture du fichier.", e);
        }
        return relativePath;
    }

    @Override
    public Resource loadAsResource(String relativePath) {
        Path target = resolve(relativePath);
        if (!Files.isReadable(target)) {
            throw new MediaStorageException("Fichier introuvable sur le disque : " + relativePath, null);
        }
        return new PathResource(target);
    }

    @Override
    public void delete(String relativePath) {
        try {
            Files.deleteIfExists(resolve(relativePath));
        } catch (IOException e) {
            throw new MediaStorageException("Échec de la suppression du fichier : " + relativePath, e);
        }
    }

    private Path resolve(String relativePath) {
        Path target = root.resolve(relativePath).normalize();
        if (!target.startsWith(root)) {
            throw new MediaStorageException("Chemin de fichier invalide : " + relativePath, null);
        }
        return target;
    }
}
