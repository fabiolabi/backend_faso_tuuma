package bf.annuaire.artisans.media.service;

import bf.annuaire.artisans.auth.security.AuthPrincipal;
import bf.annuaire.artisans.common.exception.BadRequestException;
import bf.annuaire.artisans.common.exception.ResourceNotFoundException;
import bf.annuaire.artisans.media.config.MediaProperties;
import bf.annuaire.artisans.media.entity.MediaFile;
import bf.annuaire.artisans.media.repository.MediaFileRepository;
import bf.annuaire.artisans.user.entity.User;
import bf.annuaire.artisans.user.entity.RoleName;
import bf.annuaire.artisans.user.repository.UserRepository;
import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import org.springframework.core.io.Resource;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

/**
 * Orchestration du stockage de fichiers : validation (type/taille), écriture disque via
 * {@link MediaStorageBackend}, persistance de la ligne {@code media_file}, lecture et suppression.
 */
@Service
public class MediaService {

    /** Extension de fichier dérivée du type MIME (seuls ces types sont autorisés). */
    private static final Map<String, String> EXTENSION_BY_TYPE = Map.ofEntries(
            Map.entry("image/jpeg", ".jpg"),
            Map.entry("image/jpg", ".jpg"),
            Map.entry("image/png", ".png"),
            Map.entry("image/webp", ".webp"),
            Map.entry("image/gif", ".gif"),
            Map.entry("image/heic", ".heic"),
            Map.entry("image/heif", ".heif"));

    private final MediaFileRepository mediaFileRepository;
    private final UserRepository userRepository;
    private final MediaStorageBackend storage;
    private final MediaProperties properties;

    public MediaService(
            MediaFileRepository mediaFileRepository,
            UserRepository userRepository,
            MediaStorageBackend storage,
            MediaProperties properties) {
        this.mediaFileRepository = mediaFileRepository;
        this.userRepository = userRepository;
        this.storage = storage;
        this.properties = properties;
    }

    /**
     * Valide puis stocke un fichier uploadé, et crée le {@link MediaFile} associé.
     *
     * @throws BadRequestException si le fichier est vide, d'un type non autorisé ou trop volumineux.
     */
    @Transactional
    public MediaFile upload(MultipartFile file, Long userId) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("Aucun fichier fourni.");
        }
        String contentType = resolveContentType(file);
        if (!properties.getAllowedContentTypes().contains(contentType)) {
            throw new BadRequestException(
                    "Type de fichier non autorisé. Formats acceptés : JPEG, PNG, WebP, GIF, HEIC.");
        }
        if (file.getSize() > properties.getMaxFileSizeBytes()) {
            throw new BadRequestException(
                    "Fichier trop volumineux (max " + properties.getMaxFileSizeBytes() + " octets).");
        }

        String storedPath;
        try {
            storedPath = storage.store(
                    file.getInputStream(),
                    EXTENSION_BY_TYPE.get(contentType),
                    contentType,
                    file.getSize());
        } catch (IOException e) {
            throw new BadRequestException("Lecture du fichier impossible.");
        }

        try {
            User uploader = userRepository.getReferenceById(userId);
            MediaFile mediaFile = new MediaFile(
                    sanitizeName(file.getOriginalFilename()),
                    storedPath,
                    contentType,
                    file.getSize(),
                    uploader,
                    Instant.now());
            return mediaFileRepository.save(mediaFile);
        } catch (RuntimeException e) {
            // Évite un binaire orphelin si la persistance échoue.
            storage.delete(storedPath);
            throw e;
        }
    }

    @Transactional(readOnly = true)
    public MediaFile getMetadata(Long id) {
        return mediaFileRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Fichier introuvable : " + id));
    }

    /** Charge les métadonnées et le contenu binaire pour le téléchargement. */
    @Transactional(readOnly = true)
    public MediaContent loadContent(Long id) {
        MediaFile mediaFile = getMetadata(id);
        return new MediaContent(mediaFile, storage.loadAsResource(mediaFile.getStoredPath()));
    }

    /**
     * Supprime le fichier (disque + ligne). Autorisé à l'auteur de l'upload ou à un administrateur.
     *
     * @throws AccessDeniedException si l'appelant n'est ni l'auteur ni administrateur.
     */
    @Transactional
    public void delete(Long id, AuthPrincipal principal) {
        MediaFile mediaFile = getMetadata(id);
        if (!isOwnerOrAdmin(mediaFile, principal)) {
            throw new AccessDeniedException("Suppression non autorisée.");
        }
        mediaFileRepository.delete(mediaFile);
        storage.delete(mediaFile.getStoredPath());
    }

    private boolean isOwnerOrAdmin(MediaFile mediaFile, AuthPrincipal principal) {
        if (principal.roles().contains(RoleName.ADMIN.name())) {
            return true;
        }
        User uploader = mediaFile.getUploadedBy();
        return uploader != null && uploader.getId().equals(principal.userId());
    }

    private String resolveContentType(MultipartFile file) {
        String contentType = file.getContentType();
        if (contentType != null && properties.getAllowedContentTypes().contains(contentType)) {
            return contentType;
        }
        if ("image/jpg".equals(contentType)) {
            return "image/jpeg";
        }

        String ext = StringUtils.getFilenameExtension(file.getOriginalFilename());
        if (ext != null) {
            String normalized = switch (ext.toLowerCase()) {
                case "jpg", "jpeg" -> "image/jpeg";
                case "png" -> "image/png";
                case "webp" -> "image/webp";
                case "gif" -> "image/gif";
                case "heic" -> "image/heic";
                case "heif" -> "image/heif";
                default -> contentType;
            };
            if (normalized != null && properties.getAllowedContentTypes().contains(normalized)) {
                return normalized;
            }
        }
        return contentType;
    }

    private String sanitizeName(String originalName) {
        String name = StringUtils.getFilename(originalName);
        if (!StringUtils.hasText(name)) {
            return "fichier";
        }
        return name.length() > 255 ? name.substring(0, 255) : name;
    }

    /** Couple métadonnées + contenu binaire renvoyé au controller pour le téléchargement. */
    public record MediaContent(MediaFile mediaFile, Resource resource) {}
}
