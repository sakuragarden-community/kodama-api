package it.sakura.garden.kodamaapi.event.service.impl;

import it.sakura.garden.kodamaapi.common.exception.ResourceInUseException;
import it.sakura.garden.kodamaapi.common.exception.ResourceNotFoundException;
import it.sakura.garden.kodamaapi.common.web.PageResponse;
import it.sakura.garden.kodamaapi.event.dto.EventTemplateImage;
import it.sakura.garden.kodamaapi.event.dto.EventTemplateRequest;
import it.sakura.garden.kodamaapi.event.dto.EventTemplateResponse;
import it.sakura.garden.kodamaapi.event.mapper.EventTemplateMapper;
import it.sakura.garden.kodamaapi.event.model.Event;
import it.sakura.garden.kodamaapi.event.model.EventTemplate;
import it.sakura.garden.kodamaapi.event.repository.EventEditionRepository;
import it.sakura.garden.kodamaapi.event.repository.EventRepository;
import it.sakura.garden.kodamaapi.event.repository.EventTemplateRepository;
import it.sakura.garden.kodamaapi.event.service.EventTemplateService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Base64;

/** Implementazione di {@link EventTemplateService}, con accesso ai dati delegato ai repository. */
@Service
@Transactional(readOnly = true)
public class EventTemplateServiceImpl implements EventTemplateService {

    private static final String RESOURCE = "EventTemplate";

    private static final String DATA_URI_PREFIX = "data:";
    private static final String BASE64_MARKER = ";base64,";

    private static final Logger log = LoggerFactory.getLogger(EventTemplateServiceImpl.class);

    private final EventTemplateRepository eventTemplateRepository;
    private final EventEditionRepository eventEditionRepository;
    private final EventRepository eventRepository;
    private final EventTemplateMapper eventTemplateMapper;

    public EventTemplateServiceImpl(EventTemplateRepository eventTemplateRepository,
                                    EventEditionRepository eventEditionRepository,
                                    EventRepository eventRepository,
                                    EventTemplateMapper eventTemplateMapper) {
        this.eventTemplateRepository = eventTemplateRepository;
        this.eventEditionRepository = eventEditionRepository;
        this.eventRepository = eventRepository;
        this.eventTemplateMapper = eventTemplateMapper;
    }

    @Override
    public PageResponse<EventTemplateResponse> findAll(Pageable pageable) {
        return PageResponse.from(eventTemplateRepository.findAll(pageable), eventTemplateMapper::toResponse);
    }

    @Override
    public EventTemplateResponse findById(Integer id) {
        return eventTemplateMapper.toResponse(requireById(id));
    }

    @Override
    public EventTemplateImage findImage(Integer id) {
        // Il formato è garantito dalla validazione in scrittura:
        // data:<content-type>;base64,<dati>
        String dataUri = requireById(id).getDiscordEventImage();
        int marker = dataUri.indexOf(BASE64_MARKER);

        String contentType = dataUri.substring(DATA_URI_PREFIX.length(), marker);
        byte[] data = Base64.getDecoder().decode(dataUri.substring(marker + BASE64_MARKER.length()));

        return new EventTemplateImage(contentType, data);
    }

    @Override
    @Transactional
    public EventTemplateResponse create(EventTemplateRequest request) {
        EventTemplate saved = eventTemplateRepository.save(
                eventTemplateMapper.toEntity(request, requireEvent(request.eventId())));
        log.info("Creato event template id={} eventId={}", saved.getId(), request.eventId());

        return eventTemplateMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public EventTemplateResponse update(Integer id, EventTemplateRequest request) {
        EventTemplate template = requireById(id);

        eventTemplateMapper.merge(template, request, requireEvent(request.eventId()));
        log.info("Aggiornato event template id={}", id);

        return eventTemplateMapper.toResponse(template);
    }

    @Override
    @Transactional
    public void delete(Integer id) {
        EventTemplate template = requireById(id);

        if (eventEditionRepository.existsByEventTemplateId(id)) {
            throw ResourceInUseException.of(RESOURCE, id, "EventEdition");
        }

        eventTemplateRepository.delete(template);
        log.info("Eliminato event template id={}", id);
    }

    private EventTemplate requireById(Integer id) {
        return eventTemplateRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of(RESOURCE, "id", id));
    }

    private Event requireEvent(Integer eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> ResourceNotFoundException.of("Event", "id", eventId));
    }
}
