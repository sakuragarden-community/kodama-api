package it.sakura.garden.kodamaapi.common.exception;

/**
 * Sollevata quando si tenta di violare un vincolo di unicità applicativo
 * (es. {@code discord_id} o {@code code} già presenti). Tradotta in
 * {@code 409 Conflict} dal {@code GlobalExceptionHandler}.
 */
public class DuplicateResourceException extends RuntimeException {

    public DuplicateResourceException(String message) {
        super(message);
    }

    public static DuplicateResourceException of(String resource, String field, Object value) {
        return new DuplicateResourceException(
                "%s con %s '%s' è già presente".formatted(resource, field, value));
    }
}
