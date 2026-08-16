package it.sakura.garden.kodamaapi.auth.service.impl;

import it.sakura.garden.kodamaapi.auth.model.Role;
import it.sakura.garden.kodamaapi.auth.model.User;
import it.sakura.garden.kodamaapi.auth.repository.UserRepository;
import it.sakura.garden.kodamaapi.auth.service.UserService;
import it.sakura.garden.kodamaapi.common.exception.DuplicateResourceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

/** Implementazione di {@link UserService}. */
@Service
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public boolean exists(String clientId) {
        return userRepository.existsByClientId(clientId);
    }

    @Override
    @Transactional
    public User registerClient(String clientId, String rawClientSecret, String displayName, Set<Role> roles) {
        if (userRepository.existsByClientId(clientId)) {
            throw DuplicateResourceException.of("User", "clientId", clientId);
        }

        User user = new User(clientId, passwordEncoder.encode(rawClientSecret), displayName, roles);
        User saved = userRepository.save(user);
        log.info("Registrato client '{}' con ruoli {}", saved.getClientId(), saved.getRoles());

        return saved;
    }
}
