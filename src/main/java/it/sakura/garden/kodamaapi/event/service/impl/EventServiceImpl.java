package it.sakura.garden.kodamaapi.event.service.impl;

import it.sakura.garden.kodamaapi.common.exception.ResourceInUseException;
import it.sakura.garden.kodamaapi.common.exception.ResourceNotFoundException;
import it.sakura.garden.kodamaapi.common.web.PageResponse;
import it.sakura.garden.kodamaapi.event.dto.EventRequest;
import it.sakura.garden.kodamaapi.event.dto.EventResponse;
import it.sakura.garden.kodamaapi.event.mapper.EventMapper;
import it.sakura.garden.kodamaapi.event.model.Event;
import it.sakura.garden.kodamaapi.event.repository.EventRepository;
import it.sakura.garden.kodamaapi.event.repository.EventTemplateRepository;
import it.sakura.garden.kodamaapi.event.service.EventService;
import it.sakura.garden.kodamaapi.section.model.Section;
import it.sakura.garden.kodamaapi.section.repository.SectionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Implementazione di {@link EventService}, con accesso ai dati delegato ai repository. */
@Service
@Transactional(readOnly = true)
public class EventServiceImpl implements EventService {

    private static final String RESOURCE = "Event";

    private static final Logger log = LoggerFactory.getLogger(EventServiceImpl.class);

    private final EventRepository eventRepository;
    private final EventTemplateRepository eventTemplateRepository;
    private final SectionRepository sectionRepository;
    private final EventMapper eventMapper;

    public EventServiceImpl(EventRepository eventRepository, EventTemplateRepository eventTemplateRepository,
                            SectionRepository sectionRepository, EventMapper eventMapper) {
        this.eventRepository = eventRepository;
        this.eventTemplateRepository = eventTemplateRepository;
        this.sectionRepository = sectionRepository;
        this.eventMapper = eventMapper;
    }

    @Override
    public PageResponse<EventResponse> findAll(Pageable pageable) {
        return PageResponse.from(eventRepository.findAll(pageable), eventMapper::toResponse);
    }

    @Override
    public EventResponse findById(Integer id) {
        return eventMapper.toResponse(requireById(id));
    }

    @Override
    @Transactional
    public EventResponse create(EventRequest request) {
        Event saved = eventRepository.save(eventMapper.toEntity(request, requireSection(request.sectionId())));
        log.info("Creato event id={} name={}", saved.getId(), saved.getName());

        return eventMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public EventResponse update(Integer id, EventRequest request) {
        Event event = requireById(id);

        eventMapper.merge(event, request, requireSection(request.sectionId()));
        log.info("Aggiornato event id={}", id);

        return eventMapper.toResponse(event);
    }

    @Override
    @Transactional
    public void delete(Integer id) {
        Event event = requireById(id);

        // Controllo esplicito: il vincolo di chiave esterna scatterebbe solo al
        // commit, con un messaggio generico.
        if (eventTemplateRepository.existsByEventId(id)) {
            throw ResourceInUseException.of(RESOURCE, id, "EventTemplate");
        }

        eventRepository.delete(event);
        log.info("Eliminato event id={}", id);
    }

    private Event requireById(Integer id) {
        return eventRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of(RESOURCE, "id", id));
    }

    private Section requireSection(Integer sectionId) {
        return sectionRepository.findById(sectionId)
                .orElseThrow(() -> ResourceNotFoundException.of("Section", "id", sectionId));
    }
}
