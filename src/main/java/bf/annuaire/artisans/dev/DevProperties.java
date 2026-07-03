package bf.annuaire.artisans.dev;

import org.springframework.boot.context.properties.ConfigurationProperties;

/** Paramètres du seed de données de démo (désactivé par défaut en production). */
@ConfigurationProperties(prefix = "app.dev")
public record DevProperties(boolean seedEnabled, String seedToken) {

    public DevProperties {
        if (seedToken == null) {
            seedToken = "";
        }
    }
}
