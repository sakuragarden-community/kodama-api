package it.sakura.garden.kodamaapi.auth.model;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;

/**
 * Utenza applicativa che consuma le API.
 *
 * <p>Modella un <em>client macchina</em> OAuth 2.0 (in primis il bot Discord),
 * non una persona: si autentica con {@code clientId} / {@code clientSecret} via
 * grant {@code client_credentials} e riceve un JWT.
 *
 * <p>È volutamente disgiunta da {@code Member}: quest'ultima descrive le persone
 * della community e non deve avere alcuna responsabilità di autenticazione.
 */
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "client_id", nullable = false, unique = true, length = 100)
    private String clientId;

    /** Segreto in forma di hash BCrypt: mai persistito né esposto in chiaro. */
    @Column(name = "client_secret", nullable = false, length = 255)
    private String clientSecret;

    @Column(name = "display_name", length = 150)
    private String displayName;

    @Column(name = "enabled", nullable = false)
    private boolean enabled = true;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 32)
    private Set<Role> roles = EnumSet.noneOf(Role.class);

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    protected User() {
        // richiesto da JPA
    }

    public User(String clientId, String hashedClientSecret, String displayName, Set<Role> roles) {
        this.clientId = clientId;
        this.clientSecret = hashedClientSecret;
        this.displayName = displayName;
        this.roles = roles.isEmpty() ? EnumSet.noneOf(Role.class) : EnumSet.copyOf(roles);
    }

    @PrePersist
    void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public String getClientId() {
        return clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    /** @param hashedClientSecret segreto già codificato dal {@code PasswordEncoder}. */
    public void setClientSecret(String hashedClientSecret) {
        this.clientSecret = hashedClientSecret;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public Set<Role> getRoles() {
        return Set.copyOf(roles);
    }

    public void setRoles(Set<Role> roles) {
        this.roles = roles.isEmpty() ? EnumSet.noneOf(Role.class) : EnumSet.copyOf(roles);
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        return other instanceof User user && Objects.equals(clientId, user.clientId);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(clientId);
    }

    /** Il segreto è deliberatamente escluso per non finire nei log. */
    @Override
    public String toString() {
        return "User{id=%d, clientId='%s', enabled=%s, roles=%s}".formatted(id, clientId, enabled, roles);
    }
}
