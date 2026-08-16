package it.sakura.garden.kodamaapi.config;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.KeyUse;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimNames;
import org.springframework.security.oauth2.jwt.JwtClaimValidator;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtIssuerValidator;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import java.util.List;

/**
 * Emissione e verifica dei JWT.
 *
 * <p>L'applicazione è insieme authorization server (emette i token sul grant
 * {@code client_credentials}) e resource server (li verifica sulle API): il
 * {@link JwtEncoder} serve al primo ruolo, il {@link JwtDecoder} al secondo.
 */
@Configuration(proxyBeanMethods = false)
public class JwtConfig {

    /** Chiave di firma esposta anche come JWKS, così che il {@code kid} sia risolvibile. */
    @Bean
    public JWKSource<SecurityContext> jwkSource(JwtKeyProvider keyProvider, SecurityProperties properties) {
        RSAKey rsaKey = new RSAKey.Builder(keyProvider.publicKey())
                .privateKey(keyProvider.privateKey())
                .keyID(properties.jwt().keyId())
                .keyUse(KeyUse.SIGNATURE)
                .algorithm(JWSAlgorithm.RS256)
                .build();

        return new ImmutableJWKSet<>(new JWKSet(rsaKey));
    }

    @Bean
    public JwtEncoder jwtEncoder(JWKSource<SecurityContext> jwkSource) {
        return new NimbusJwtEncoder(jwkSource);
    }

    /**
     * Oltre ai controlli di default (firma e scadenza) verifica esplicitamente
     * {@code iss} e {@code aud}: senza il secondo, un token emesso per un altro
     * servizio ma firmato con la stessa chiave verrebbe accettato.
     */
    @Bean
    public JwtDecoder jwtDecoder(JwtKeyProvider keyProvider, SecurityProperties properties) {
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withPublicKey(keyProvider.publicKey())
                .signatureAlgorithm(SignatureAlgorithm.RS256)
                .build();

        String expectedAudience = properties.jwt().audience();
        OAuth2TokenValidator<Jwt> audienceValidator = new JwtClaimValidator<List<String>>(
                JwtClaimNames.AUD,
                audience -> audience != null && audience.contains(expectedAudience));

        decoder.setJwtValidator(JwtValidators.createDefaultWithValidators(
                new JwtIssuerValidator(properties.jwt().issuer()),
                audienceValidator));

        return decoder;
    }
}
