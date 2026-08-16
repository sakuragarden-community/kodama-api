package it.sakura.garden.kodamaapi.member.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.Objects;

/**
 * Membro della community Kodama Nest, identificato univocamente dal proprio
 * snowflake Discord.
 *
 * <p>L'istanziazione passa dal {@link Builder}: il costruttore senza argomenti
 * resta {@code protected} perché richiesto da JPA, ma non fa parte dell'API
 * pubblica del dominio.
 */
@Entity
@Table(name = "members")
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /** Snowflake Discord: numerico a 64 bit, trattato come stringa. */
    @Column(name = "discord_id", nullable = false, unique = true, length = 32)
    private String discordId;

    @Column(name = "username", nullable = false, length = 100)
    private String username;

    /** Momento in cui il record è stato creato lato API, non lato Discord. */
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    /** Ingresso nel server Discord. */
    @Column(name = "joined_at")
    private Instant joinedAt;

    /** Uscita dal server Discord; {@code null} finché il membro è presente. */
    @Column(name = "left_at")
    private Instant leftAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    private MemberStatus status;

    @Column(name = "presentation", columnDefinition = "text")
    private String presentation;

    @Column(name = "experience", nullable = false)
    private int experience;

    protected Member() {
        // richiesto da JPA
    }

    private Member(Builder builder) {
        this.discordId = builder.discordId;
        this.username = builder.username;
        this.joinedAt = builder.joinedAt;
        this.leftAt = builder.leftAt;
        this.status = builder.status;
        this.presentation = builder.presentation;
        this.experience = builder.experience;
    }

    public static Builder builder() {
        return new Builder();
    }

    /**
     * Applica i default previsti dalle specifiche prima del primo salvataggio,
     * così che restino garantiti indipendentemente da chi costruisce l'entità.
     */
    @PrePersist
    void applyDefaults() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
        if (status == null) {
            status = MemberStatus.ACTIVE;
        }
    }

    public Integer getId() {
        return id;
    }

    public String getDiscordId() {
        return discordId;
    }

    public void setDiscordId(String discordId) {
        this.discordId = discordId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getJoinedAt() {
        return joinedAt;
    }

    public void setJoinedAt(Instant joinedAt) {
        this.joinedAt = joinedAt;
    }

    public Instant getLeftAt() {
        return leftAt;
    }

    public void setLeftAt(Instant leftAt) {
        this.leftAt = leftAt;
    }

    public MemberStatus getStatus() {
        return status;
    }

    public void setStatus(MemberStatus status) {
        this.status = status;
    }

    public String getPresentation() {
        return presentation;
    }

    public void setPresentation(String presentation) {
        this.presentation = presentation;
    }

    public int getExperience() {
        return experience;
    }

    public void setExperience(int experience) {
        this.experience = experience;
    }

    /**
     * Uguaglianza basata sulla chiave naturale ({@code discordId}) anziché
     * sull'id generato, così che il contratto resti stabile anche per istanze
     * non ancora persistite.
     */
    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        return other instanceof Member member && Objects.equals(discordId, member.discordId);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(discordId);
    }

    @Override
    public String toString() {
        return "Member{id=%d, discordId='%s', username='%s', status=%s}"
                .formatted(id, discordId, username, status);
    }

    /** Builder del dominio: unica via di costruzione di un {@link Member} nuovo. */
    public static final class Builder {

        private String discordId;
        private String username;
        private Instant joinedAt;
        private Instant leftAt;
        private MemberStatus status = MemberStatus.ACTIVE;
        private String presentation;
        private int experience;

        private Builder() {
        }

        public Builder discordId(String discordId) {
            this.discordId = discordId;
            return this;
        }

        public Builder username(String username) {
            this.username = username;
            return this;
        }

        public Builder joinedAt(Instant joinedAt) {
            this.joinedAt = joinedAt;
            return this;
        }

        public Builder leftAt(Instant leftAt) {
            this.leftAt = leftAt;
            return this;
        }

        public Builder status(MemberStatus status) {
            this.status = status;
            return this;
        }

        public Builder presentation(String presentation) {
            this.presentation = presentation;
            return this;
        }

        public Builder experience(int experience) {
            this.experience = experience;
            return this;
        }

        public Member build() {
            return new Member(this);
        }
    }
}
