package it.sakura.garden.kodamaapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.security.autoconfigure.UserDetailsServiceAutoConfiguration;

/**
 * Le utenze vivono nella tabella {@code users} e si autenticano sul token
 * endpoint: l'utente in memoria con password generata che Spring Boot
 * creerebbe di default non servirebbe a nulla, perciò la sua auto-configurazione
 * viene esclusa.
 */
@SpringBootApplication(exclude = UserDetailsServiceAutoConfiguration.class)
public class KodamaApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(KodamaApiApplication.class, args);
    }

}
