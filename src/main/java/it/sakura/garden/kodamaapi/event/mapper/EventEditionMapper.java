package it.sakura.garden.kodamaapi.event.mapper;

import it.sakura.garden.kodamaapi.event.dto.EventEditionRequest;
import it.sakura.garden.kodamaapi.event.dto.EventEditionResponse;
import it.sakura.garden.kodamaapi.event.model.EventEdition;
import it.sakura.garden.kodamaapi.event.model.EventTemplate;
import org.springframework.stereotype.Component;

/**
 * Factory e proiettore per {@link EventEdition}. Come {@link EventMapper}, non
 * implementa {@code EntityMapper}: il template referenziato arriva dal service.
 */
@Component
public class EventEditionMapper {

    public EventEdition toEntity(EventEditionRequest request, EventTemplate template) {
        return new EventEdition(template, request.plannedAt(), request.discordEventId());
    }

    public void merge(EventEdition edition, EventEditionRequest request, EventTemplate template) {
        edition.setEventTemplate(template);
        edition.setPlannedAt(request.plannedAt());
        edition.setDiscordEventId(request.discordEventId());
    }

    public EventEditionResponse toResponse(EventEdition edition) {
        // getId() su un proxy LAZY non ne forza il caricamento
        return new EventEditionResponse(edition.getId(), edition.getEventTemplate().getId(),
                edition.getPlannedAt(), edition.getDiscordEventId());
    }
}
