package bf.annuaire.artisans.common.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.Instant;
import java.util.List;

/**
 * Corps de réponse d'erreur standardisé renvoyé par {@link GlobalExceptionHandler}.
 *
 * <p>{@code timestamp} est sérialisé en UTC ISO-8601 (cf. configuration Jackson). {@code fieldErrors}
 * n'est présent que pour les erreurs de validation.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiError(
        Instant timestamp,
        int status,
        String error,
        String message,
        String path,
        List<FieldValidationError> fieldErrors) {

    public static ApiError of(int status, String error, String message, String path) {
        return new ApiError(Instant.now(), status, error, message, path, null);
    }

    public static ApiError of(
            int status, String error, String message, String path, List<FieldValidationError> fieldErrors) {
        return new ApiError(Instant.now(), status, error, message, path, fieldErrors);
    }

    /** Détail d'une erreur de validation sur un champ précis. */
    public record FieldValidationError(String field, String message) {}
}
