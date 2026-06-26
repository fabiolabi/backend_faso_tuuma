package bf.annuaire.artisans.media.config;

import java.util.Set;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Propriétés de configuration du stockage des fichiers, liées au préfixe {@code app.media}
 * (cf. {@code application.properties}).
 */
@Component
@ConfigurationProperties(prefix = "app.media")
@Getter
@Setter
public class MediaProperties {

    /** Répertoire racine où sont écrits les binaires (relatif au répertoire de lancement). */
    private String storageDir = "./data/media";

    /** Taille maximale acceptée pour un fichier, en octets. */
    private long maxFileSizeBytes = 5_242_880; // 5 Mo

    /** Types MIME autorisés à l'upload (images uniquement). */
    private Set<String> allowedContentTypes = Set.of("image/jpeg", "image/png", "image/webp");
}
