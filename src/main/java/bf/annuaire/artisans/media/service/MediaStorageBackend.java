package bf.annuaire.artisans.media.service;

import java.io.InputStream;
import org.springframework.core.io.Resource;

/**
 * Abstraction du stockage binaire des fichiers media (disque local ou Cloudflare R2).
 *
 * <p>Le chemin relatif ({@code storedPath}, ex. {@code 2026/06/uuid.jpg}) est généré côté serveur
 * et persisté en base ; il sert de clé d'objet sur R2.
 */
public interface MediaStorageBackend {

    /**
     * Écrit le contenu et renvoie le chemin relatif (séparateurs {@code /}) à persister.
     *
     * @param sizeBytes taille connue du flux (octets), utilisée par R2 pour l'upload.
     */
    String store(InputStream content, String extension, String contentType, long sizeBytes);

    /** Charge le fichier en ressource lisible pour le téléchargement. */
    Resource loadAsResource(String relativePath);

    /** Supprime le fichier (best-effort : l'absence n'est pas une erreur). */
    void delete(String relativePath);
}
