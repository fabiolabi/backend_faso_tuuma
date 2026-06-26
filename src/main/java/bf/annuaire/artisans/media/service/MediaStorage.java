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
import org.springframework.core.io.PathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

/**
 * I/O bas niveau du stockage des fichiers sur le système de fichiers local.
 *
 * <p>Le chemin relatif ({@code storedPath}) est entièrement généré côté serveur
 * ({@code yyyy/MM/uuid.ext}) : il n'est jamais dérivé du nom fourni par le client, ce qui élimine
 * tout risque de <em>path traversal</em>. Toutes les résolutions sont vérifiées pour rester sous le
 * répertoire racine.
 */
@Component
public class MediaStorage {

    private static final DateTimeFormatter SHARD =
            DateTimeFormatter.ofPattern("yyyy/MM").withZone(ZoneOffset.UTC);

    private final Path root;

    public MediaStorage(MediaProperties properties) {
        this.root = Paths.get(properties.getStorageDir()).toAbsolutePath().normalize();
    }

    /**
     * Écrit le contenu sous {@code yyyy/MM/uuid.ext} et renvoie le chemin relatif (séparateurs
     * {@code /}) à persister.
     */
    public String store(InputStream content, String extension) {
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

    /** Charge le fichier en ressource lisible pour le téléchargement. */
    public Resource loadAsResource(String relativePath) {
        Path target = resolve(relativePath);
        if (!Files.isReadable(target)) {
            throw new MediaStorageException("Fichier introuvable sur le disque : " + relativePath, null);
        }
        return new PathResource(target);
    }

    /** Supprime le fichier (best-effort : l'absence n'est pas une erreur). */
    public void delete(String relativePath) {
        try {
            Files.deleteIfExists(resolve(relativePath));
        } catch (IOException e) {
            throw new MediaStorageException("Échec de la suppression du fichier : " + relativePath, e);
        }
    }

    /** Résout un chemin relatif sous {@link #root} en garantissant qu'il n'en sort pas. */
    private Path resolve(String relativePath) {
        Path target = root.resolve(relativePath).normalize();
        if (!target.startsWith(root)) {
            throw new MediaStorageException("Chemin de fichier invalide : " + relativePath, null);
        }
        return target;
    }

    /** Erreur d'I/O du stockage, mappée en 500 par le gestionnaire global. */
    public static class MediaStorageException extends RuntimeException {
        public MediaStorageException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
