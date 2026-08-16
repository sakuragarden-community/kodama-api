package it.sakura.garden.kodamaapi.auth.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Errore del token endpoint nel formato previsto da RFC 6749 §5.2.
 *
 * <p>Volutamente diverso da {@code ApiError}: sul token endpoint il formato è
 * dettato dallo standard, così che qualsiasi client OAuth 2.0 lo interpreti
 * senza codice dedicato.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record OAuth2ErrorResponse(
        @JsonProperty("error") String error,
        @JsonProperty("error_description") String errorDescription) {
}
