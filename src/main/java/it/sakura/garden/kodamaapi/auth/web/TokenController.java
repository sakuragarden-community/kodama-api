package it.sakura.garden.kodamaapi.auth.web;

import it.sakura.garden.kodamaapi.auth.dto.OAuth2ErrorResponse;
import it.sakura.garden.kodamaapi.auth.dto.TokenResponse;
import it.sakura.garden.kodamaapi.auth.exception.OAuth2TokenException;
import it.sakura.garden.kodamaapi.auth.model.ClientCredentials;
import it.sakura.garden.kodamaapi.auth.service.ClientCredentialsExtractor;
import it.sakura.garden.kodamaapi.auth.service.TokenService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Token endpoint OAuth 2.0.
 *
 * <p>Espone il solo grant {@code client_credentials}, che è quello previsto per
 * un client macchina come il bot Discord: non esiste un utente finale davanti
 * al browser da cui raccogliere un consenso, quindi i flussi interattivi
 * (authorization code) non si applicano.
 *
 * <pre>
 * POST /oauth2/token
 * Authorization: Basic base64(clientId:clientSecret)
 * Content-Type: application/x-www-form-urlencoded
 *
 * grant_type=client_credentials
 * </pre>
 */
@RestController
@RequestMapping("/oauth2")
public class TokenController {

    private static final String CLIENT_CREDENTIALS_GRANT = "client_credentials";

    private static final String REALM = "Basic realm=\"kodama-api\"";

    private final TokenService tokenService;
    private final ClientCredentialsExtractor credentialsExtractor;

    public TokenController(TokenService tokenService, ClientCredentialsExtractor credentialsExtractor) {
        this.tokenService = tokenService;
        this.credentialsExtractor = credentialsExtractor;
    }

    @PostMapping(
            path = "/token",
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<TokenResponse> token(
            @RequestParam(name = "grant_type", required = false) String grantType,
            @RequestParam(name = "client_id", required = false) String clientId,
            @RequestParam(name = "client_secret", required = false) String clientSecret,
            @RequestHeader(name = HttpHeaders.AUTHORIZATION, required = false) String authorizationHeader) {

        if (!StringUtils.hasText(grantType)) {
            throw OAuth2TokenException.invalidRequest("Il parametro grant_type è obbligatorio");
        }
        if (!CLIENT_CREDENTIALS_GRANT.equals(grantType)) {
            throw OAuth2TokenException.unsupportedGrantType(grantType);
        }

        ClientCredentials credentials =
                credentialsExtractor.extract(authorizationHeader, clientId, clientSecret);

        // RFC 6749 §5.1: la risposta contiene credenziali e non va mai messa in cache.
        return ResponseEntity.ok()
                .cacheControl(org.springframework.http.CacheControl.noStore())
                .header(HttpHeaders.PRAGMA, "no-cache")
                .body(tokenService.issueClientCredentialsToken(credentials));
    }

    /**
     * Handler locale al controller: ha la precedenza sul
     * {@code GlobalExceptionHandler} e garantisce che il token endpoint
     * risponda nel formato di errore previsto dallo standard OAuth 2.0.
     */
    @ExceptionHandler(OAuth2TokenException.class)
    public ResponseEntity<OAuth2ErrorResponse> handleTokenError(OAuth2TokenException exception) {
        boolean invalidClient = OAuth2TokenException.INVALID_CLIENT.equals(exception.getErrorCode());
        HttpStatus status = invalidClient ? HttpStatus.UNAUTHORIZED : HttpStatus.BAD_REQUEST;

        ResponseEntity.BodyBuilder response = ResponseEntity.status(status)
                .cacheControl(org.springframework.http.CacheControl.noStore());

        if (invalidClient) {
            response.header(HttpHeaders.WWW_AUTHENTICATE, REALM);
        }

        return response.body(new OAuth2ErrorResponse(exception.getErrorCode(), exception.getMessage()));
    }
}
