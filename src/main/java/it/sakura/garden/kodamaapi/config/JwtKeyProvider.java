package it.sakura.garden.kodamaapi.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.converter.RsaKeyConverters;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

/**
 * Sorgente della coppia di chiavi RSA usata per firmare e verificare i JWT.
 *
 * <p>Se le chiavi sono configurate vengono lette dai PEM indicati; altrimenti ne
 * viene generata una effimera in memoria. La seconda modalità serve solo allo
 * sviluppo: a ogni riavvio la chiave cambia e i token emessi prima smettono di
 * essere validi, perciò l'evento viene segnalato a livello {@code WARN}.
 */
@Component
public class JwtKeyProvider {

    private static final Logger log = LoggerFactory.getLogger(JwtKeyProvider.class);

    private static final int KEY_SIZE_BITS = 2048;

    private final RSAPublicKey publicKey;
    private final RSAPrivateKey privateKey;

    public JwtKeyProvider(SecurityProperties properties) {
        SecurityProperties.Jwt jwt = properties.jwt();

        if (jwt.hasConfiguredKeyPair()) {
            this.publicKey = readPublicKey(jwt);
            this.privateKey = readPrivateKey(jwt);
            log.info("Chiavi di firma JWT caricate dalla configurazione (kid={})", jwt.keyId());
        } else {
            KeyPair keyPair = generateKeyPair();
            this.publicKey = (RSAPublicKey) keyPair.getPublic();
            this.privateKey = (RSAPrivateKey) keyPair.getPrivate();
            log.warn("Nessuna chiave JWT configurata: ne è stata generata una effimera. "
                    + "I token emessi non sopravvivranno al riavvio. "
                    + "In produzione valorizzare kodama.security.jwt.private-key-location e public-key-location.");
        }
    }

    public RSAPublicKey publicKey() {
        return publicKey;
    }

    public RSAPrivateKey privateKey() {
        return privateKey;
    }

    private static RSAPublicKey readPublicKey(SecurityProperties.Jwt jwt) {
        try (InputStream in = jwt.publicKeyLocation().getInputStream()) {
            return RsaKeyConverters.x509().convert(in);
        } catch (IOException exception) {
            throw new IllegalStateException(
                    "Impossibile leggere la chiave pubblica JWT da " + jwt.publicKeyLocation(), exception);
        }
    }

    private static RSAPrivateKey readPrivateKey(SecurityProperties.Jwt jwt) {
        try (InputStream in = jwt.privateKeyLocation().getInputStream()) {
            return RsaKeyConverters.pkcs8().convert(in);
        } catch (IOException exception) {
            throw new IllegalStateException(
                    "Impossibile leggere la chiave privata JWT da " + jwt.privateKeyLocation(), exception);
        }
    }

    private static KeyPair generateKeyPair() {
        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(KEY_SIZE_BITS);
            return generator.generateKeyPair();
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("RSA non disponibile nella JVM", exception);
        }
    }
}
