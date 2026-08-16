package it.sakura.garden.kodamaapi.common.exception;

/**
 * Sollevata dal service layer quando una risorsa richiesta non esiste.
 * Tradotta in {@code 404 Not Found} dal {@code GlobalExceptionHandler}: il
 * dominio non conosce i codici HTTP, se ne occupa il solo layer web.
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    /** @return eccezione del tipo «Member con id 42 non trovato». */
    public static ResourceNotFoundException of(String resource, String field, Object value) {
        return new ResourceNotFoundException(
                "%s con %s '%s' non trovato".formatted(resource, field, value));
    }
}
