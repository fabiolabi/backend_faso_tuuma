package bf.annuaire.artisans.media.service;

/** Erreur d'I/O du stockage media, mappée en 500 par le gestionnaire global. */
public class MediaStorageException extends RuntimeException {

    public MediaStorageException(String message, Throwable cause) {
        super(message, cause);
    }
}
