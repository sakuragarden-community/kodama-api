package it.sakura.garden.kodamaapi.auth.service;

import it.sakura.garden.kodamaapi.auth.exception.OAuth2TokenException;
import it.sakura.garden.kodamaapi.auth.model.ClientCredentials;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Estrae le credenziali del client dalla richiesta al token endpoint.
 *
 * <p>RFC 6749 §2.3.1 ammette due modalità e dà la precedenza all'header
 * {@code Authorization: Basic}; il corpo della richiesta resta supportato come
 * alternativa. Isolare qui la logica evita di spargere il parsing nel
 * controller e la rende verificabile da sola.
 */
@Component
public class ClientCredentialsExtractor {

    private static final String BASIC_PREFIX = "Basic ";

    /**
     * @param authorizationHeader header {@code Authorization}, se presente
     * @param formClientId        {@code client_id} dal corpo, se presente
     * @param formClientSecret    {@code client_secret} dal corpo, se presente
     * @throws OAuth2TokenException se non è possibile ricavare credenziali complete
     */
    public ClientCredentials extract(String authorizationHeader, String formClientId, String formClientSecret) {
        if (StringUtils.hasText(authorizationHeader)
                && authorizationHeader.regionMatches(true, 0, BASIC_PREFIX, 0, BASIC_PREFIX.length())) {
            return fromBasicHeader(authorizationHeader.substring(BASIC_PREFIX.length()).trim());
        }

        if (StringUtils.hasText(formClientId) && StringUtils.hasText(formClientSecret)) {
            return new ClientCredentials(formClientId, formClientSecret);
        }

        throw OAuth2TokenException.invalidRequest(
                "Credenziali del client mancanti: usare l'header Basic oppure i parametri client_id e client_secret");
    }

    private ClientCredentials fromBasicHeader(String encoded) {
        String decoded;
        try {
            decoded = new String(Base64.getDecoder().decode(encoded), StandardCharsets.UTF_8);
        } catch (IllegalArgumentException exception) {
            throw OAuth2TokenException.invalidRequest("Header Authorization Basic non decodificabile");
        }

        int separator = decoded.indexOf(':');
        if (separator < 0) {
            throw OAuth2TokenException.invalidRequest("Header Authorization Basic malformato");
        }

        // RFC 6749 §2.3.1: i due valori viaggiano codificati in application/
        // x-www-form-urlencoded prima della Base64, quindi vanno decodificati.
        String clientId = urlDecode(decoded.substring(0, separator));
        String clientSecret = urlDecode(decoded.substring(separator + 1));

        if (!StringUtils.hasText(clientId) || !StringUtils.hasText(clientSecret)) {
            throw OAuth2TokenException.invalidRequest("Header Authorization Basic incompleto");
        }

        return new ClientCredentials(clientId, clientSecret);
    }

    private String urlDecode(String value) {
        try {
            return URLDecoder.decode(value, StandardCharsets.UTF_8);
        } catch (IllegalArgumentException exception) {
            throw OAuth2TokenException.invalidRequest("Credenziali del client con codifica non valida");
        }
    }
}
