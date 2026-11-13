package it.sakura.garden.kodamaapi.event.model;

import it.sakura.garden.kodamaapi.section.model.Section;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * Tipologia di evento organizzato dallo Staff, legata a una {@link Section}.
 * Le singole date in cui si svolge sono le {@link EventEdition} dei suoi
 * {@link EventTemplate}.
 */
@Entity
@Table(name = "events")
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "section_id", nullable = false)
    private Section section;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    /** ID del ruolo Discord di riferimento: snowflake, trattato come stringa. */
    @Column(name = "interest", nullable = false, length = 32)
    private String interest;

    /** Numero massimo di partecipanti; {@code 0} indica nessun limite. */
    @Column(name = "max_participants", nullable = false)
    private int maxParticipants;

    /** {@code false} se la partecipazione richiede un'iscrizione. */
    @Column(name = "is_open", nullable = false)
    private boolean open;

    protected Event() {
        // richiesto da JPA
    }

    public Event(Section section, String name, String interest, int maxParticipants, boolean open) {
        this.section = section;
        this.name = name;
        this.interest = interest;
        this.maxParticipants = maxParticipants;
        this.open = open;
    }

    public Integer getId() {
        return id;
    }

    public Section getSection() {
        return section;
    }

    public void setSection(Section section) {
        this.section = section;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getInterest() {
        return interest;
    }

    public void setInterest(String interest) {
        this.interest = interest;
    }

    public int getMaxParticipants() {
        return maxParticipants;
    }

    public void setMaxParticipants(int maxParticipants) {
        this.maxParticipants = maxParticipants;
    }

    public boolean isOpen() {
        return open;
    }

    public void setOpen(boolean open) {
        this.open = open;
    }

    // Senza una chiave naturale l'identità è quella del database: due entità
    // non ancora salvate non sono mai uguali.
    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        return other instanceof Event event && id != null && id.equals(event.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "Event{id=%d, name='%s'}".formatted(id, name);
    }
}
