package it.sakura.garden.kodamaapi.event.repository;

import it.sakura.garden.kodamaapi.event.model.Event;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/** Accesso alla persistenza per {@link Event}. */
@Repository
public interface EventRepository extends JpaRepository<Event, Integer> {

    /** Carica anche la sezione, proiettata in ogni risposta, evitando una query per riga. */
    @Override
    @EntityGraph(attributePaths = "section")
    Page<Event> findAll(Pageable pageable);
}
