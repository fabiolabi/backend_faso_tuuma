package bf.annuaire.artisans.common.exception;

import jakarta.validation.ConstraintViolationException;
import java.util.List;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/**
 * Gestionnaire global des exceptions : transforme toute exception en réponse {@link ApiError}
 * cohérente (DTO léger, dates UTC ISO-8601), adaptée au client mobile.
 *
 * <p>Étend {@link ResponseEntityExceptionHandler} pour que les exceptions du framework Spring MVC
 * (404 ressource introuvable, 405 méthode non supportée, 400 corps illisible, validation…)
 * conservent leur statut HTTP correct au lieu d'être uniformisées en 500.
 */
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    // ---------- Exceptions métier ----------

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiError> handleNotFound(ResourceNotFoundException ex, WebRequest request) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage(), request);
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ApiError> handleBadRequest(BadRequestException ex, WebRequest request) {
        return build(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiError> handleConstraintViolation(
            ConstraintViolationException ex, WebRequest request) {
        List<ApiError.FieldValidationError> fieldErrors = ex.getConstraintViolations().stream()
                .map(v -> new ApiError.FieldValidationError(
                        v.getPropertyPath().toString(), v.getMessage()))
                .toList();
        ApiError body = ApiError.of(
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                "La validation de la requête a échoué.",
                path(request),
                fieldErrors);
        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ApiError> handleUnauthorized(UnauthorizedException ex, WebRequest request) {
        return build(HttpStatus.UNAUTHORIZED, ex.getMessage(), request);
    }

    /** Échec d'authentification Spring Security (mauvais identifiants, compte désactivé…) -> 401. */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiError> handleAuthentication(AuthenticationException ex, WebRequest request) {
        return build(HttpStatus.UNAUTHORIZED, "Identifiants invalides.", request);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiError> handleAccessDenied(AccessDeniedException ex, WebRequest request) {
        return build(HttpStatus.FORBIDDEN, "Accès refusé.", request);
    }

    /** Filet de sécurité : toute exception non prévue devient un 500 propre. */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleUnexpected(Exception ex, WebRequest request) {
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Une erreur interne est survenue.", request);
    }

    // ---------- Exceptions du framework Spring MVC ----------

    /** Surcharge la validation {@code @Valid} pour exposer les erreurs par champ. */
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {
        List<ApiError.FieldValidationError> fieldErrors = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> new ApiError.FieldValidationError(fe.getField(), fe.getDefaultMessage()))
                .toList();
        ApiError body = ApiError.of(
                status.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                "La validation de la requête a échoué.",
                path(request),
                fieldErrors);
        return new ResponseEntity<>(body, headers, status);
    }

    /** Fichier uploadé dépassant la limite multipart -> 413 (message FR). */
    @Override
    protected ResponseEntity<Object> handleMaxUploadSizeExceededException(
            MaxUploadSizeExceededException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {
        ApiError body = ApiError.of(
                HttpStatus.PAYLOAD_TOO_LARGE.value(),
                HttpStatus.PAYLOAD_TOO_LARGE.getReasonPhrase(),
                "Fichier trop volumineux (max 5 Mo).",
                path(request));
        return new ResponseEntity<>(body, headers, HttpStatus.PAYLOAD_TOO_LARGE);
    }

    /**
     * Point de passage unique des exceptions framework : produit un {@link ApiError} avec le
     * statut HTTP d'origine (404, 405, 400, 415…).
     */
    @Override
    protected ResponseEntity<Object> handleExceptionInternal(
            Exception ex,
            Object body,
            HttpHeaders headers,
            HttpStatusCode statusCode,
            WebRequest request) {
        HttpStatus status = HttpStatus.resolve(statusCode.value());
        String reason = status != null ? status.getReasonPhrase() : "Error";
        ApiError apiError = ApiError.of(statusCode.value(), reason, ex.getMessage(), path(request));
        return new ResponseEntity<>(apiError, headers, statusCode);
    }

    // ---------- Helpers ----------

    private ResponseEntity<ApiError> build(HttpStatus status, String message, WebRequest request) {
        ApiError body = ApiError.of(status.value(), status.getReasonPhrase(), message, path(request));
        return ResponseEntity.status(status).body(body);
    }

    private String path(WebRequest request) {
        if (request instanceof ServletWebRequest servletWebRequest) {
            return servletWebRequest.getRequest().getRequestURI();
        }
        return request.getDescription(false).replaceFirst("^uri=", "");
    }
}
