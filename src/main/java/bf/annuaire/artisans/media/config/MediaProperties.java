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



    /**

     * Backend de stockage : {@code local} (disque) ou {@code r2} (Cloudflare R2 via API S3).

     */

    private String backend = "local";



    /** Répertoire racine où sont écrits les binaires (backend {@code local} uniquement). */

    private String storageDir = "./data/media";



    /**

     * URL publique de base pour servir les images directement (domaine R2 public ou CDN).

     * Ex. {@code https://media.fasotuuma.bf}. Si vide, les URLs restent {@code /api/media/{id}}.

     */

    private String publicBaseUrl = "";



    /** Taille maximale acceptée pour un fichier, en octets. */

    private long maxFileSizeBytes = 5_242_880; // 5 Mo



    /** Types MIME autorisés à l'upload (images uniquement). */

    private Set<String> allowedContentTypes = Set.of(

            "image/jpeg",

            "image/jpg",

            "image/png",

            "image/webp",

            "image/gif",

            "image/heic",

            "image/heif");



    /** Paramètres Cloudflare R2 (backend {@code r2} uniquement). */

    private R2Properties r2 = new R2Properties();

    /**
     * Migration one-shot : copie les fichiers locaux ({@code storageDir}) vers R2 au démarrage.
     */
    private boolean migrateLocalToR2 = false;



    @Getter

    @Setter

    public static class R2Properties {



        /** Nom du bucket R2. */

        private String bucket = "";



        /**

         * Endpoint S3 R2, ex. {@code https://<account_id>.r2.cloudflarestorage.com}.

         */

        private String endpoint = "";



        /** Clé d'accès R2 (API token). */

        private String accessKeyId = "";



        /** Secret R2 (API token). */

        private String secretAccessKey = "";



        /** Région S3 pour R2 (toujours {@code auto}). */

        private String region = "auto";

    }

}

