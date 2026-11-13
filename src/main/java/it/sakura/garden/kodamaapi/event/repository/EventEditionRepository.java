package it.sakura.garden.kodamaapi.event.repository;

import it.sakura.garden.kodamaapi.event.model.EventEdition;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;

/** Accesso alla persistenza per {@link EventEdition}. */
@Repository
public interface EventEditionRepository extends JpaRepository<EventEdition, Integer> {

    /** Edizioni pianificate nell'intervallo semiaperto {@code [from, to)}. */
    Page<EventEdition> findByPlannedAtGreaterThanEqualAndPlannedAtLessThan(Instant from, Instant to,
                                                                           Pageable pageable);

    boolean existsByEventTemplateId(Integer eventTemplateId);
}
