package bf.annuaire.artisans.media.dto;

import java.time.Instant;

/**
 * Représentation publique légère d'un {@link bf.annuaire.artisans.media.entity.MediaFile} (jamais
 * l'entité exposée). {@code url} pointe vers l'endpoint de téléchargement du backend
 * ({@code /api/media/{id}}), le fichier étant servi par l'API.
 */
public record MediaFileDto(
        Long id,
        String url,
        String originalName,
        String contentType,
        long sizeBytes,
        Instant createdAt) {}
