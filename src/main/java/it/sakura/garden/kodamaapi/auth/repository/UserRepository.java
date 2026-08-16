package it.sakura.garden.kodamaapi.auth.repository;

import it.sakura.garden.kodamaapi.auth.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/** Accesso alla persistenza per le utenze applicative {@link User}. */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByClientId(String clientId);

    boolean existsByClientId(String clientId);
}
