package it.sakura.garden.kodamaapi.auth.bootstrap;

import it.sakura.garden.kodamaapi.auth.service.UserService;
import it.sakura.garden.kodamaapi.config.SecurityProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.security.SecureRandom;
import java.util.Base64;

/**
 * Crea al primo avvio l'utenza con cui il bot Discord ottiene i token.
 *
 * <p>Senza questo passo la tabella {@code users} resterebbe vuota e nessuna
 * chiamata potrebbe essere autenticata. L'operazione è idempotente: se il
 * client esiste già non viene toccato, così un riavvio non ne sovrascrive mai
 * il segreto.
 */
@Component
public class BootstrapClientInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(BootstrapClientInitializer.class);

    private static final int GENERATED_SECRET_BYTES = 32;

    private final UserService userService;
    private final SecurityProperties.Bootstrap bootstrap;

    public BootstrapClientInitializer(UserService userService, SecurityProperties securityProperties) {
        this.userService = userService;
        this.bootstrap = securityProperties.bootstrap();
    }

    @Override
    public void run(ApplicationArguments args) {
        if (!bootstrap.enabled()) {
            log.debug("Bootstrap del client disabilitato");
            return;
        }

        String clientId = bootstrap.clientId();
        if (userService.exists(clientId)) {
            log.debug("Client '{}' già presente: bootstrap saltato", clientId);
            return;
        }

        boolean generated = !StringUtils.hasText(bootstrap.clientSecret());
        String secret = generated ? generateSecret() : bootstrap.clientSecret();

        userService.registerClient(clientId, secret, "Bootstrap client", bootstrap.roles());

        if (generated) {
            // Stampato una sola volta, come fa Spring Boot con la password di
            // default: è l'unico momento in cui il valore è recuperabile.
            log.warn("""

                    ------------------------------------------------------------
                    Creato il client OAuth 2.0 di bootstrap
                      client_id     : {}
                      client_secret : {}
                    Segreto generato automaticamente e mostrato solo ora.
                    Configurare kodama.security.bootstrap.client-secret per fissarlo.
                    ------------------------------------------------------------
                    """, clientId, secret);
        } else {
            log.info("Creato il client OAuth 2.0 di bootstrap '{}' con il segreto configurato", clientId);
        }
    }

    private String generateSecret() {
        byte[] bytes = new byte[GENERATED_SECRET_BYTES];
        new SecureRandom().nextBytes(bytes);

        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
