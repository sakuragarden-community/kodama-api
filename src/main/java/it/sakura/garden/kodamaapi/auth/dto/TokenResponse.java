package it.sakura.garden.kodamaapi.auth.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Risposta del token endpoint nel formato previsto da RFC 6749 §5.1.
 *
 * <p>I nomi in {@code snake_case} sono imposti dallo standard, per questo sono
 * dichiarati esplicitamente con {@link JsonProperty}.
 *
 * <p>Il grant {@code client_credentials} non prevede refresh token (RFC 6749
 * §4.4.3): il client si limita a richiedere un nuovo access token alla scadenza.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record TokenResponse(
        @JsonProperty("access_token") String accessToken,
        @JsonProperty("token_type") String tokenType,
        @JsonProperty("expires_in") long expiresIn,
        @JsonProperty("scope") String scope) {

    public static final String BEARER_TOKEN_TYPE = "Bearer";

    public static TokenResponse bearer(String accessToken, long expiresInSeconds, String scope) {
        return new TokenResponse(accessToken, BEARER_TOKEN_TYPE, expiresInSeconds, scope);
    }
}
