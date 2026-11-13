package it.sakura.garden.kodamaapi.event.service;

import it.sakura.garden.kodamaapi.common.web.PageResponse;
import it.sakura.garden.kodamaapi.event.dto.EventTemplateImage;
import it.sakura.garden.kodamaapi.event.dto.EventTemplateRequest;
import it.sakura.garden.kodamaapi.event.dto.EventTemplateResponse;
import org.springframework.data.domain.Pageable;

/** Logica di business dei template evento, inclusa la gestione dell'immagine. */
public interface EventTemplateService {

    PageResponse<EventTemplateResponse> findAll(Pageable pageable);

    EventTemplateResponse findById(Integer id);

    /** Immagine dell'evento Discord, decodificata dal data URI salvato. */
    EventTemplateImage findImage(Integer id);

    EventTemplateResponse create(EventTemplateRequest request);

    EventTemplateResponse update(Integer id, EventTemplateRequest request);

    /** @throws it.sakura.garden.kodamaapi.common.exception.ResourceInUseException se il template ha delle edizioni */
    void delete(Integer id);
}
