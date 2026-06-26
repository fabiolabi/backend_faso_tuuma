package bf.annuaire.artisans.common.exception;

/** Levée lorsqu'une authentification échoue ou qu'un jeton est invalide. Mappée en HTTP 401. */
public class UnauthorizedException extends RuntimeException {

    public UnauthorizedException(String message) {
        super(message);
    }
}
