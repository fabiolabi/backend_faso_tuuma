package bf.annuaire.artisans.common.exception;

/** Levée lorsqu'une requête est invalide d'un point de vue métier. Mappée en HTTP 400. */
public class BadRequestException extends RuntimeException {

    public BadRequestException(String message) {
        super(message);
    }
}
