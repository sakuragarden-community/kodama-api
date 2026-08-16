package it.sakura.garden.kodamaapi.auth.exception;

/**
 * Errore del token endpoint, portatore di uno dei codici standard di
 * RFC 6749 §5.2.
 *
 * <p>Non conosce lo status HTTP: la traduzione avviene nel controller, che è
 * l'unico layer a cui compete il protocollo di trasporto.
 */
public class OAuth2TokenException extends RuntimeException {

    public static final String INVALID_REQUEST = "invalid_request";
    public static final String INVALID_CLIENT = "invalid_client";
    public static final String UNSUPPORTED_GRANT_TYPE = "unsupported_grant_type";

    private final String errorCode;

    private OAuth2TokenException(String errorCode, String description) {
        super(description);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public static OAuth2TokenException invalidRequest(String description) {
        return new OAuth2TokenException(INVALID_REQUEST, description);
    }

    /**
     * Il messaggio resta volutamente generico: distinguere «client inesistente»
     * da «segreto errato» permetterebbe di enumerare le utenze valide.
     */
    public static OAuth2TokenException invalidClient() {
        return new OAuth2TokenException(INVALID_CLIENT, "Autenticazione del client fallita");
    }

    public static OAuth2TokenException unsupportedGrantType(String grantType) {
        return new OAuth2TokenException(UNSUPPORTED_GRANT_TYPE,
                "Il grant type '%s' non è supportato".formatted(grantType));
    }
}
