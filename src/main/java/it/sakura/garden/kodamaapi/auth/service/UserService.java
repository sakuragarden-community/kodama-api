package it.sakura.garden.kodamaapi.auth.service;

import it.sakura.garden.kodamaapi.auth.model.Role;
import it.sakura.garden.kodamaapi.auth.model.User;
import it.sakura.garden.kodamaapi.common.exception.DuplicateResourceException;

import java.util.Set;

/** Gestione delle utenze applicative che possono ottenere un token. */
public interface UserService {

    boolean exists(String clientId);

    /**
     * Registra un nuovo client. Il segreto arriva in chiaro e viene codificato
     * qui: è l'unico punto del sistema che lo vede prima dell'hashing.
     *
     * @throws DuplicateResourceException se il {@code clientId} è già registrato
     */
    User registerClient(String clientId, String rawClientSecret, String displayName, Set<Role> roles);
}
