package it.sakura.garden.kodamaapi.auth.service.impl;

import it.sakura.garden.kodamaapi.auth.dto.TokenResponse;
import it.sakura.garden.kodamaapi.auth.exception.OAuth2TokenException;
import it.sakura.garden.kodamaapi.auth.model.ClientCredentials;
import it.sakura.garden.kodamaapi.auth.model.Role;
import it.sakura.garden.kodamaapi.auth.model.User;
import it.sakura.garden.kodamaapi.auth.repository.UserRepository;
import it.sakura.garden.kodamaapi.auth.service.TokenService;
import it.sakura.garden.kodamaapi.config.SecurityProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Implementazione di {@link TokenService} che emette JWT firmati in RS256.
 *
 * <p>Il token è autoconsistente: porta con sé i ruoli del client, così che la
 * validazione sulle API non richieda un altro giro sul database.
 */
@Service
public class JwtTokenService implements TokenService {

    private static final Logger log = LoggerFactory.getLogger(JwtTokenService.class);

    /** Claim non standard con i ruoli, letto dal converter del resource server. */
    private static final String ROLES_CLAIM = "roles";

    private static final String CLIENT_NAME_CLAIM = "client_name";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtEncoder jwtEncoder;
    private final SecurityProperties.Jwt jwtProperties;

    /**
     * Hash inservibile usato quando il client non esiste: verificarlo comunque
     * mantiene costante il tempo di risposta ed evita che la latenza riveli
     * quali {@code clientId} sono registrati.
     */
    private final String dummyHash;

    public JwtTokenService(UserRepository userRepository,
                           PasswordEncoder passwordEncoder,
                           JwtEncoder jwtEncoder,
                           SecurityProperties securityProperties) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtEncoder = jwtEncoder;
        this.jwtProperties = securityProperties.jwt();
        this.dummyHash = passwordEncoder.encode(UUID.randomUUID().toString());
    }

    @Override
    @Transactional(readOnly = true)
    public TokenResponse issueClientCredentialsToken(ClientCredentials credentials) {
        User user = authenticate(credentials);

        Duration ttl = jwtProperties.accessTokenTtl();
        Instant issuedAt = Instant.now();
        Instant expiresAt = issuedAt.plus(ttl);

        List<String> roles = user.getRoles().stream().map(Role::name).sorted().toList();
        String scope = String.join(" ", roles);

        JwtClaimsSet.Builder claims = JwtClaimsSet.builder()
                .issuer(jwtProperties.issuer())
                .audience(List.of(jwtProperties.audience()))
                .subject(user.getClientId())
                .issuedAt(issuedAt)
                .expiresAt(expiresAt)
                .id(UUID.randomUUID().toString())
                .claim(ROLES_CLAIM, roles)
                .claim("scope", scope);

        if (StringUtils.hasText(user.getDisplayName())) {
            claims.claim(CLIENT_NAME_CLAIM, user.getDisplayName());
        }

        JwsHeader header = JwsHeader.with(SignatureAlgorithm.RS256)
                .keyId(jwtProperties.keyId())
                .build();

        String token = jwtEncoder.encode(JwtEncoderParameters.from(header, claims.build())).getTokenValue();
        log.info("Emesso access token per client '{}' con ruoli {}", user.getClientId(), roles);

        return TokenResponse.bearer(token, ttl.toSeconds(), scope);
    }

    private User authenticate(ClientCredentials credentials) {
        Optional<User> candidate = userRepository.findByClientId(credentials.clientId());

        String expectedHash = candidate.map(User::getClientSecret).orElse(dummyHash);
        boolean secretMatches = passwordEncoder.matches(credentials.clientSecret(), expectedHash);

        User user = candidate
                .filter(found -> secretMatches && found.isEnabled())
                .orElseThrow(() -> {
                    log.warn("Autenticazione fallita per clientId '{}'", credentials.clientId());
                    return OAuth2TokenException.invalidClient();
                });

        if (user.getRoles().isEmpty()) {
            // Un token senza ruoli non aprirebbe nulla: meglio un errore
            // esplicito che un 403 inspiegabile alla prima chiamata.
            log.warn("Il client '{}' non ha ruoli assegnati", user.getClientId());
            throw OAuth2TokenException.invalidClient();
        }

        return user;
    }
}
