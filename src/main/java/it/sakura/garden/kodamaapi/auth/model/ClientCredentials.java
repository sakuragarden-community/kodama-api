package it.sakura.garden.kodamaapi.auth.model;

/**
 * Credenziali presentate da un client sul grant {@code client_credentials},
 * indipendentemente dal fatto che siano arrivate nell'header {@code Basic} o
 * nel corpo della richiesta.
 */
public record ClientCredentials(String clientId, String clientSecret) {

    @Override
    public String toString() {
        return "ClientCredentials{clientId='%s'}".formatted(clientId);
    }
}
