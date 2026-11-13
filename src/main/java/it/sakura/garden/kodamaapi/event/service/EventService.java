package it.sakura.garden.kodamaapi.event.service;

import it.sakura.garden.kodamaapi.common.web.PageResponse;
import it.sakura.garden.kodamaapi.event.dto.EventRequest;
import it.sakura.garden.kodamaapi.event.dto.EventResponse;
import org.springframework.data.domain.Pageable;

/** Logica di business degli eventi, inclusa la risoluzione della sezione referenziata. */
public interface EventService {

    PageResponse<EventResponse> findAll(Pageable pageable);

    EventResponse findById(Integer id);

    EventResponse create(EventRequest request);

    EventResponse update(Integer id, EventRequest request);

    /** @throws it.sakura.garden.kodamaapi.common.exception.ResourceInUseException se l'evento ha dei template */
    void delete(Integer id);
}
