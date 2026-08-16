package it.sakura.garden.kodamaapi.auth.web;

import com.nimbusds.jose.jwk.JWKMatcher;
import com.nimbusds.jose.jwk.JWKSelector;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Pubblica la chiave pubblica di firma in formato JWKS.
 *
 * <p>Serve a chiunque debba verificare i token emessi da questa applicazione
 * senza condividerne la configurazione: un eventuale secondo servizio del
 * Kodama Nest può puntare qui come {@code jwk-set-uri} invece di ricevere una
 * copia della chiave.
 */
@RestController
@RequestMapping("/oauth2")
public class JwkSetController {

    private final JWKSource<SecurityContext> jwkSource;

    public JwkSetController(JWKSource<SecurityContext> jwkSource) {
        this.jwkSource = jwkSource;
    }

    @GetMapping(path = "/jwks", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> keys() throws Exception {
        JWKSelector selector = new JWKSelector(new JWKMatcher.Builder().build());

        // toPublicJWKSet() rimuove i parametri privati: senza, la chiave di
        // firma finirebbe in chiaro su un endpoint pubblico.
        return new JWKSet(jwkSource.get(selector, null)).toPublicJWKSet().toJSONObject();
    }
}
