package it.sakura.garden.kodamaapi.common.exception;

/**
 * Sollevata quando si tenta di eliminare una risorsa ancora referenziata da
 * altre (es. un evento con dei template). Tradotta in {@code 409 Conflict} dal
 * {@code GlobalExceptionHandler}.
 */
public class ResourceInUseException extends RuntimeException {

    public ResourceInUseException(String message) {
        super(message);
    }

    /** @return eccezione del tipo «Event con id '3' è ancora usato da almeno un EventTemplate». */
    public static ResourceInUseException of(String resource, Object id, String dependent) {
        return new ResourceInUseException(
                "%s con id '%s' è ancora usato da almeno un %s".formatted(resource, id, dependent));
    }
}
