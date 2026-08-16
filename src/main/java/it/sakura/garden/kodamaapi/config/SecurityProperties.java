package it.sakura.garden.kodamaapi.config;

import it.sakura.garden.kodamaapi.auth.model.Role;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.core.io.Resource;

import java.time.Duration;
import java.util.Set;

/**
 * Configurazione della sicurezza sotto il prefisso {@code kodama.security}.
 *
 * <p>Tutti i valori hanno un default utilizzabile in sviluppo, così che
 * l'applicazione parta senza configurazione aggiuntiva; in produzione vanno
 * sovrascritti — in particolare le chiavi di firma e il segreto del bot.
 */
@ConfigurationProperties(prefix = "kodama.security")
public record SecurityProperties(@DefaultValue Jwt jwt, @DefaultValue Bootstrap bootstrap) {

    /**
     * Parametri di emissione e validazione dei JWT.
     *
     * @param issuer          valore del claim {@code iss}, verificato in ingresso
     * @param audience        valore del claim {@code aud}, verificato in ingresso
     * @param keyId           {@code kid} pubblicato nel JWKS e scritto nell'header
     * @param accessTokenTtl  durata dell'access token
     * @param privateKeyLocation chiave privata PKCS#8 in PEM; se assente ne viene
     *                           generata una effimera a ogni avvio
     * @param publicKeyLocation  chiave pubblica X.509 in PEM
     */
    public record Jwt(
            @DefaultValue("kodama-api") String issuer,
            @DefaultValue("kodama-api") String audience,
            @DefaultValue("kodama-signing-key") String keyId,
            @DefaultValue("1h") Duration accessTokenTtl,
            Resource privateKeyLocation,
            Resource publicKeyLocation) {

        /** Vero solo se entrambe le chiavi sono state configurate esplicitamente. */
        public boolean hasConfiguredKeyPair() {
            return privateKeyLocation != null && publicKeyLocation != null;
        }
    }

    /**
     * Utenza creata al primo avvio, per non dover popolare a mano la tabella
     * {@code users} prima di poter chiamare le API.
     *
     * @param clientSecret se omesso ne viene generato uno casuale, stampato una
     *                     sola volta a log come avviene per la password di
     *                     default di Spring Boot
     */
    public record Bootstrap(
            @DefaultValue("true") boolean enabled,
            @DefaultValue("discord-bot") String clientId,
            String clientSecret,
            @DefaultValue("BOT") Set<Role> roles) {
    }
}
