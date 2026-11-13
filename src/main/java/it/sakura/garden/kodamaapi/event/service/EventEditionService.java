package it.sakura.garden.kodamaapi.event.service;

import it.sakura.garden.kodamaapi.common.web.PageResponse;
import it.sakura.garden.kodamaapi.event.dto.EventEditionRequest;
import it.sakura.garden.kodamaapi.event.dto.EventEditionResponse;
import org.springframework.data.domain.Pageable;

import java.time.Instant;

/** Logica di business delle edizioni evento, cioè delle date pianificate. */
public interface EventEditionService {

    /**
     * Lista paginata, filtrabile per data pianificata nell'intervallo semiaperto
     * {@code [from, to)}; un estremo nullo non limita la ricerca da quel lato.
     */
    PageResponse<EventEditionResponse> findAll(Instant from, Instant to, Pageable pageable);

    EventEditionResponse findById(Integer id);

    EventEditionResponse create(EventEditionRequest request);

    EventEditionResponse update(Integer id, EventEditionRequest request);

    void delete(Integer id);
}
