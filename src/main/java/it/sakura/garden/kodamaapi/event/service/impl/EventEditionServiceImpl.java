package it.sakura.garden.kodamaapi.event.service.impl;

import it.sakura.garden.kodamaapi.common.exception.ResourceNotFoundException;
import it.sakura.garden.kodamaapi.common.web.PageResponse;
import it.sakura.garden.kodamaapi.event.dto.EventEditionRequest;
import it.sakura.garden.kodamaapi.event.dto.EventEditionResponse;
import it.sakura.garden.kodamaapi.event.mapper.EventEditionMapper;
import it.sakura.garden.kodamaapi.event.model.EventEdition;
import it.sakura.garden.kodamaapi.event.model.EventTemplate;
import it.sakura.garden.kodamaapi.event.repository.EventEditionRepository;
import it.sakura.garden.kodamaapi.event.repository.EventTemplateRepository;
import it.sakura.garden.kodamaapi.event.service.EventEditionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

/** Implementazione di {@link EventEditionService}, con accesso ai dati delegato ai repository. */
@Service
@Transactional(readOnly = true)
public class EventEditionServiceImpl implements EventEditionService {

    private static final String RESOURCE = "EventEdition";

    /** Estremi usati al posto di un filtro assente: compresi nel range di {@code TIMESTAMPTZ}. */
    private static final Instant MIN_PLANNED_AT = Instant.EPOCH;
    private static final Instant MAX_PLANNED_AT = Instant.parse("9999-12-31T23:59:59Z");

    private static final Logger log = LoggerFactory.getLogger(EventEditionServiceImpl.class);

    private final EventEditionRepository eventEditionRepository;
    private final EventTemplateRepository eventTemplateRepository;
    private final EventEditionMapper eventEditionMapper;

    public EventEditionServiceImpl(EventEditionRepository eventEditionRepository,
                                   EventTemplateRepository eventTemplateRepository,
                                   EventEditionMapper eventEditionMapper) {
        this.eventEditionRepository = eventEditionRepository;
        this.eventTemplateRepository = eventTemplateRepository;
        this.eventEditionMapper = eventEditionMapper;
    }

    @Override
    public PageResponse<EventEditionResponse> findAll(Instant from, Instant to, Pageable pageable) {
        Page<EventEdition> page = from == null && to == null
                ? eventEditionRepository.findAll(pageable)
                : eventEditionRepository.findByPlannedAtGreaterThanEqualAndPlannedAtLessThan(
                        from != null ? from : MIN_PLANNED_AT,
                        to != null ? to : MAX_PLANNED_AT,
                        pageable);

        return PageResponse.from(page, eventEditionMapper::toResponse);
    }

    @Override
    public EventEditionResponse findById(Integer id) {
        return eventEditionMapper.toResponse(requireById(id));
    }

    @Override
    @Transactional
    public EventEditionResponse create(EventEditionRequest request) {
        EventEdition saved = eventEditionRepository.save(
                eventEditionMapper.toEntity(request, requireTemplate(request.eventTemplateId())));
        log.info("Creata event edition id={} eventTemplateId={} plannedAt={}",
                saved.getId(), request.eventTemplateId(), saved.getPlannedAt());

        return eventEditionMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public EventEditionResponse update(Integer id, EventEditionRequest request) {
        EventEdition edition = requireById(id);

        eventEditionMapper.merge(edition, request, requireTemplate(request.eventTemplateId()));
        log.info("Aggiornata event edition id={}", id);

        return eventEditionMapper.toResponse(edition);
    }

    @Override
    @Transactional
    public void delete(Integer id) {
        eventEditionRepository.delete(requireById(id));
        log.info("Eliminata event edition id={}", id);
    }

    private EventEdition requireById(Integer id) {
        return eventEditionRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of(RESOURCE, "id", id));
    }

    private EventTemplate requireTemplate(Integer eventTemplateId) {
        return eventTemplateRepository.findById(eventTemplateId)
                .orElseThrow(() -> ResourceNotFoundException.of("EventTemplate", "id", eventTemplateId));
    }
}
