package it.sakura.garden.kodamaapi.event.repository;

import it.sakura.garden.kodamaapi.event.model.EventTemplate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/** Accesso alla persistenza per {@link EventTemplate}. */
@Repository
public interface EventTemplateRepository extends JpaRepository<EventTemplate, Integer> {

    /** Carica anche l'evento, proiettato in ogni risposta, evitando una query per riga. */
    @Override
    @EntityGraph(attributePaths = "event")
    Page<EventTemplate> findAll(Pageable pageable);

    boolean existsByEventId(Integer eventId);
}
