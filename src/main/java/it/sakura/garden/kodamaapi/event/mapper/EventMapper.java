package it.sakura.garden.kodamaapi.event.mapper;

import it.sakura.garden.kodamaapi.event.dto.EventRequest;
import it.sakura.garden.kodamaapi.event.dto.EventResponse;
import it.sakura.garden.kodamaapi.event.model.Event;
import it.sakura.garden.kodamaapi.section.model.Section;
import org.springframework.stereotype.Component;

/**
 * Factory e proiettore per {@link Event}. Non implementa {@code EntityMapper}
 * perché la sezione referenziata va risolta dal service e passata insieme alla
 * richiesta.
 */
@Component
public class EventMapper {

    public Event toEntity(EventRequest request, Section section) {
        return new Event(section, request.name(), request.interest(), maxParticipants(request), request.isOpen());
    }

    public void merge(Event event, EventRequest request, Section section) {
        event.setSection(section);
        event.setName(request.name());
        event.setInterest(request.interest());
        event.setMaxParticipants(maxParticipants(request));
        event.setOpen(request.isOpen());
    }

    public EventResponse toResponse(Event event) {
        Section section = event.getSection();

        return new EventResponse(event.getId(), section.getId(), section.getName(), event.getName(),
                event.getInterest(), event.getMaxParticipants(), event.isOpen());
    }

    /** Un limite omesso equivale a nessun limite. */
    private static int maxParticipants(EventRequest request) {
        return request.maxParticipants() != null ? request.maxParticipants() : 0;
    }
}
