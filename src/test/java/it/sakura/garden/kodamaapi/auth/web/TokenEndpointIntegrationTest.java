package it.sakura.garden.kodamaapi.auth.web;

import it.sakura.garden.kodamaapi.auth.model.Role;
import it.sakura.garden.kodamaapi.auth.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.transaction.TestTransaction;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Verifica il flusso OAuth 2.0 {@code client_credentials} dal token endpoint
 * fino all'uso effettivo del token su una API protetta.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class TokenEndpointIntegrationTest {

    private static final String CLIENT_ID = "integration-test-bot";
    private static final String CLIENT_SECRET = "integration-test-secret";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserService userService;

    @Autowired
    private JwtDecoder jwtDecoder;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void registerClient() {
        if (!userService.exists(CLIENT_ID)) {
            userService.registerClient(CLIENT_ID, CLIENT_SECRET, "Client di test", Set.of(Role.BOT));
        }
    }

    @Test
    @DisplayName("credenziali valide producono un token verificabile con i ruoli del client")
    void issuesVerifiableToken() throws Exception {
        String accessToken = obtainAccessToken();

        Jwt decoded = jwtDecoder.decode(accessToken);

        assertThat(decoded.getSubject()).isEqualTo(CLIENT_ID);
        assertThat(decoded.getAudience()).contains("kodama-api");
        assertThat(decoded.getClaimAsStringList("roles")).containsExactly(Role.BOT.name());
        assertThat(decoded.getExpiresAt()).isAfter(decoded.getIssuedAt());
    }

    @Test
    @DisplayName("il token ottenuto apre effettivamente le API protette")
    void tokenGrantsAccessToProtectedApi() throws Exception {
        String accessToken = obtainAccessToken();

        mockMvc.perform(get("/api/v1/members").header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("un segreto errato produce invalid_client con status 401")
    void rejectsWrongSecret() throws Exception {
        mockMvc.perform(post("/oauth2/token")
                        .with(httpBasic(CLIENT_ID, "segreto-sbagliato"))
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("grant_type", "client_credentials"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("invalid_client"));
    }

    @Test
    @DisplayName("un client inesistente non è distinguibile da un segreto errato")
    void rejectsUnknownClient() throws Exception {
        mockMvc.perform(post("/oauth2/token")
                        .with(httpBasic("client-inesistente", "qualsiasi"))
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("grant_type", "client_credentials"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("invalid_client"));
    }

    @Test
    @DisplayName("un grant type diverso da client_credentials è rifiutato")
    void rejectsUnsupportedGrantType() throws Exception {
        mockMvc.perform(post("/oauth2/token")
                        .with(httpBasic(CLIENT_ID, CLIENT_SECRET))
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("grant_type", "authorization_code"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("unsupported_grant_type"));
    }

    @Test
    @DisplayName("senza credenziali il token endpoint risponde invalid_request")
    void rejectsMissingCredentials() throws Exception {
        mockMvc.perform(post("/oauth2/token")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("grant_type", "client_credentials"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("invalid_request"));
    }

    @Test
    @DisplayName("il JWKS è pubblico e non espone la chiave privata")
    void publishesPublicKeyOnly() throws Exception {
        mockMvc.perform(get("/oauth2/jwks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.keys[0].kty").value("RSA"))
                .andExpect(jsonPath("$.keys[0].kid").exists())
                // "d" è l'esponente privato: se comparisse, la chiave di firma
                // sarebbe scaricabile da chiunque.
                .andExpect(jsonPath("$.keys[0].d").doesNotExist());
    }

    private String obtainAccessToken() throws Exception {
        // TestTransaction non serve: il client registrato nel setUp è visibile
        // alla stessa transazione in cui gira la richiesta MockMvc.
        assertThat(TestTransaction.isActive()).isTrue();

        MvcResult result = mockMvc.perform(post("/oauth2/token")
                        .with(httpBasic(CLIENT_ID, CLIENT_SECRET))
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("grant_type", "client_credentials"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token_type").value("Bearer"))
                .andExpect(jsonPath("$.expires_in").isNumber())
                .andReturn();

        JsonNode body = objectMapper.readTree(result.getResponse().getContentAsString());

        return body.get("access_token").asString();
    }
}
