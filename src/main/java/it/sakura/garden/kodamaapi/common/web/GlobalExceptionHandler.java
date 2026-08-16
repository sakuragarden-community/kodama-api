package it.sakura.garden.kodamaapi.common.web;

import it.sakura.garden.kodamaapi.common.exception.DuplicateResourceException;
import it.sakura.garden.kodamaapi.common.exception.ResourceNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationTrustResolver;
import org.springframework.security.authentication.AuthenticationTrustResolverImpl;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.List;

/**
 * Traduce le eccezioni di dominio e di framework in risposte {@link ApiError}
 * uniformi.
 *
 * <p>È il solo punto in cui il progetto associa un errore applicativo a uno
 * status HTTP: service ed entità restano ignari del protocollo.
 *
 * <p>Gli errori di sicurezza sollevati <em>prima</em> del controller restano in
 * carico agli handler della catena di filtri; quelli sollevati <em>dentro</em>
 * il controller da {@code @PreAuthorize} arrivano invece qui e vanno gestiti
 * esplicitamente, altrimenti il ripiego su {@link Exception} li tradurrebbe in
 * un 500.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private static final AuthenticationTrustResolver TRUST_RESOLVER = new AuthenticationTrustResolverImpl();

    private static final String BEARER_CHALLENGE = "Bearer realm=\"kodama-api\"";

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiError> handleNotFound(ResourceNotFoundException exception,
                                                   HttpServletRequest request) {
        return build(HttpStatus.NOT_FOUND, exception.getMessage(), request);
    }

    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ApiError> handleDuplicate(DuplicateResourceException exception,
                                                    HttpServletRequest request) {
        return build(HttpStatus.CONFLICT, exception.getMessage(), request);
    }

    /** Violazioni di {@code @Valid} sui corpi di richiesta. */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException exception,
                                                     HttpServletRequest request) {
        List<ApiError.FieldViolation> violations = exception.getBindingResult().getFieldErrors().stream()
                .map(error -> new ApiError.FieldViolation(error.getField(), error.getDefaultMessage()))
                .toList();

        ApiError body = ApiError.of(
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                "La richiesta contiene campi non validi",
                request.getRequestURI(),
                violations);

        return ResponseEntity.badRequest().body(body);
    }

    /** JSON malformato o valore non convertibile (es. uno status inesistente). */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> handleUnreadable(HttpMessageNotReadableException exception,
                                                     HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST, "Corpo della richiesta non leggibile o malformato", request);
    }

    /** Parametro di path o query non convertibile nel tipo atteso. */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiError> handleTypeMismatch(MethodArgumentTypeMismatchException exception,
                                                       HttpServletRequest request) {
        String message = "Il parametro '%s' ha un valore non valido: '%s'"
                .formatted(exception.getName(), exception.getValue());
        return build(HttpStatus.BAD_REQUEST, message, request);
    }

    /**
     * Rete di sicurezza per i vincoli applicati dal database: il service
     * controlla l'unicità in anticipo, ma due richieste concorrenti possono
     * comunque superare quel controllo e collidere sull'indice.
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiError> handleDataIntegrity(DataIntegrityViolationException exception,
                                                        HttpServletRequest request) {
        log.warn("Violazione di integrità su {}", request.getRequestURI(), exception);
        return build(HttpStatus.CONFLICT, "L'operazione viola un vincolo di integrità dei dati", request);
    }

    /**
     * Autorizzazione negata da {@code @PreAuthorize} sul metodo del controller.
     *
     * <p>Distingue i due casi perché sono errori diversi per il client: senza
     * un'identità la risposta corretta è 401 con la sfida {@code Bearer} — il
     * bot deve richiedere un token — mentre con un'identità valida ma priva del
     * ruolo necessario è 403, e ritentare non servirebbe a nulla.
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiError> handleAccessDenied(AccessDeniedException exception,
                                                       HttpServletRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || TRUST_RESOLVER.isAnonymous(authentication)) {
            ApiError body = ApiError.of(
                    HttpStatus.UNAUTHORIZED.value(),
                    HttpStatus.UNAUTHORIZED.getReasonPhrase(),
                    "Autenticazione richiesta: fornire un access token valido nell'header Authorization",
                    request.getRequestURI());

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .header(HttpHeaders.WWW_AUTHENTICATE, BEARER_CHALLENGE)
                    .body(body);
        }

        return build(HttpStatus.FORBIDDEN, "Permessi insufficienti per questa operazione", request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleUnexpected(Exception exception, HttpServletRequest request) {
        log.error("Errore non gestito su {}", request.getRequestURI(), exception);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Errore interno del server", request);
    }

    private ResponseEntity<ApiError> build(HttpStatus status, String message, HttpServletRequest request) {
        ApiError body = ApiError.of(status.value(), status.getReasonPhrase(), message, request.getRequestURI());
        return ResponseEntity.status(status).body(body);
    }
}
