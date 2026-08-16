package it.sakura.garden.kodamaapi.auth.service;

import it.sakura.garden.kodamaapi.auth.exception.OAuth2TokenException;
import it.sakura.garden.kodamaapi.auth.model.ClientCredentials;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Verifica il parsing delle credenziali del client secondo RFC 6749 §2.3.1. */
class ClientCredentialsExtractorTest {

    private final ClientCredentialsExtractor extractor = new ClientCredentialsExtractor();

    private static String basic(String clientId, String clientSecret) {
        String raw = clientId + ":" + clientSecret;
        return "Basic " + Base64.getEncoder().encodeToString(raw.getBytes(StandardCharsets.UTF_8));
    }

    @Test
    @DisplayName("legge le credenziali dall'header Basic")
    void readsBasicHeader() {
        ClientCredentials credentials = extractor.extract(basic("discord-bot", "s3cr3t"), null, null);

        assertThat(credentials.clientId()).isEqualTo("discord-bot");
        assertThat(credentials.clientSecret()).isEqualTo("s3cr3t");
    }

    @Test
    @DisplayName("l'header Basic ha la precedenza sui parametri del corpo")
    void basicHeaderWinsOverFormParameters() {
        ClientCredentials credentials =
                extractor.extract(basic("da-header", "segreto-header"), "da-form", "segreto-form");

        assertThat(credentials.clientId()).isEqualTo("da-header");
    }

    @Test
    @DisplayName("in assenza di header usa i parametri del corpo")
    void fallsBackToFormParameters() {
        ClientCredentials credentials = extractor.extract(null, "discord-bot", "s3cr3t");

        assertThat(credentials.clientId()).isEqualTo("discord-bot");
        assertThat(credentials.clientSecret()).isEqualTo("s3cr3t");
    }

    @Test
    @DisplayName("decodifica i caratteri percent-encoded nell'header Basic")
    void decodesPercentEncodedCredentials() {
        ClientCredentials credentials = extractor.extract(basic("bot%40kodama", "a%2Bb"), null, null);

        assertThat(credentials.clientId()).isEqualTo("bot@kodama");
        assertThat(credentials.clientSecret()).isEqualTo("a+b");
    }

    @Test
    @DisplayName("senza credenziali segnala invalid_request")
    void failsWithoutCredentials() {
        assertThatThrownBy(() -> extractor.extract(null, null, null))
                .isInstanceOf(OAuth2TokenException.class)
                .extracting(exception -> ((OAuth2TokenException) exception).getErrorCode())
                .isEqualTo(OAuth2TokenException.INVALID_REQUEST);
    }

    @Test
    @DisplayName("un header Basic senza separatore è invalid_request")
    void failsOnMalformedBasicHeader() {
        String malformed = "Basic " + Base64.getEncoder()
                .encodeToString("senza-due-punti".getBytes(StandardCharsets.UTF_8));

        assertThatThrownBy(() -> extractor.extract(malformed, null, null))
                .isInstanceOf(OAuth2TokenException.class);
    }
}
