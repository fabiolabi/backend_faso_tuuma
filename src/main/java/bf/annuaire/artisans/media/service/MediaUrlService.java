package bf.annuaire.artisans.media.service;

import bf.annuaire.artisans.media.config.MediaProperties;
import bf.annuaire.artisans.media.entity.MediaFile;
import lombok.RequiredArgsConstructor;
import org.mapstruct.Named;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * Construit l'URL publique d'un fichier media.
 *
 * <p>Si {@code app.media.public-base-url} est défini (domaine R2 public ou CDN), renvoie une URL
 * absolue directe. Sinon, renvoie le chemin relatif API ({@code /api/media/{id}}) qui proxie le
 * binaire depuis le backend.
 */
@Component
@RequiredArgsConstructor
public class MediaUrlService {

    private final MediaProperties properties;

    @Named("mediaUrl")
    public String mediaUrl(MediaFile file) {
        if (file == null) {
            return null;
        }
        String base = properties.getPublicBaseUrl();
        if (StringUtils.hasText(base)) {
            String normalizedBase = base.endsWith("/") ? base.substring(0, base.length() - 1) : base;
            return normalizedBase + "/" + file.getStoredPath();
        }
        return "/api/media/" + file.getId();
    }
}
