package it.sakura.garden.kodamaapi.event.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.Instant;

/** Singola data pianificata in cui un {@link EventTemplate} si svolge. */
@Entity
@Table(name = "event_editions")
public class EventEdition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "event_template_id", nullable = false)
    private EventTemplate eventTemplate;

    @Column(name = "planned_at", nullable = false)
    private Instant plannedAt;

    /** Evento programmato creato dal bot su Discord; {@code null} finché non esiste. */
    @Column(name = "discord_event_id", length = 32)
    private String discordEventId;

    protected EventEdition() {
        // richiesto da JPA
    }

    public EventEdition(EventTemplate eventTemplate, Instant plannedAt, String discordEventId) {
        this.eventTemplate = eventTemplate;
        this.plannedAt = plannedAt;
        this.discordEventId = discordEventId;
    }

    public Integer getId() {
        return id;
    }

    public EventTemplate getEventTemplate() {
        return eventTemplate;
    }

    public void setEventTemplate(EventTemplate eventTemplate) {
        this.eventTemplate = eventTemplate;
    }

    public Instant getPlannedAt() {
        return plannedAt;
    }

    public void setPlannedAt(Instant plannedAt) {
        this.plannedAt = plannedAt;
    }

    public String getDiscordEventId() {
        return discordEventId;
    }

    public void setDiscordEventId(String discordEventId) {
        this.discordEventId = discordEventId;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        return other instanceof EventEdition edition && id != null && id.equals(edition.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "EventEdition{id=%d, plannedAt=%s}".formatted(id, plannedAt);
    }
}
