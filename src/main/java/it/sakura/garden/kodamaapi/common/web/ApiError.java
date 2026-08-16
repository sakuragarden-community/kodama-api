package it.sakura.garden.kodamaapi.common.web;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;
import java.util.List;

/**
 * Corpo di risposta uniforme per tutti gli errori delle API, così che il bot
 * Discord possa gestirli con un unico parser.
 *
 * @param violations dettaglio per-campo, presente solo sugli errori di validazione
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record ApiError(
        Instant timestamp,
        int status,
        String error,
        String message,
        String path,
        List<FieldViolation> violations) {

    public static ApiError of(int status, String error, String message, String path) {
        return new ApiError(Instant.now(), status, error, message, path, List.of());
    }

    public static ApiError of(int status, String error, String message, String path,
                              List<FieldViolation> violations) {
        return new ApiError(Instant.now(), status, error, message, path, violations);
    }

    /** Singola violazione di validazione su un campo della richiesta. */
    public record FieldViolation(String field, String message) {
    }
}
