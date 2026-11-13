package it.sakura.garden.kodamaapi.event.mapper;

import it.sakura.garden.kodamaapi.event.dto.EventTemplateRequest;
import it.sakura.garden.kodamaapi.event.dto.EventTemplateResponse;
import it.sakura.garden.kodamaapi.event.model.Event;
import it.sakura.garden.kodamaapi.event.model.EventTemplate;
import org.springframework.stereotype.Component;

/**
 * Factory e proiettore per {@link EventTemplate}. Come {@link EventMapper}, non
 * implementa {@code EntityMapper}: l'evento referenziato arriva dal service.
 */
@Component
public class EventTemplateMapper {

    public EventTemplate toEntity(EventTemplateRequest request, Event event) {
        return new EventTemplate(event, request.discordEventTitle(), request.discordEventDescription(),
                request.discordEventImage(), request.discordNewsChannel(), request.discordNewsContent(),
                threadPost(request));
    }

    public void merge(EventTemplate template, EventTemplateRequest request, Event event) {
        template.setEvent(event);
        template.setDiscordEventTitle(request.discordEventTitle());
        template.setDiscordEventDescription(request.discordEventDescription());
        template.setDiscordEventImage(request.discordEventImage());
        template.setDiscordNewsChannel(request.discordNewsChannel());
        template.setDiscordNewsContent(request.discordNewsContent());
        template.setDiscordThreadPost(threadPost(request));
    }

    public EventTemplateResponse toResponse(EventTemplate template) {
        Event event = template.getEvent();

        return new EventTemplateResponse(template.getId(), event.getId(), event.getName(),
                template.getDiscordEventTitle(), template.getDiscordEventDescription(),
                template.getDiscordNewsChannel(), template.getDiscordNewsContent(),
                template.getDiscordThreadPost());
    }

    /** Un messaggio vuoto equivale a nessun messaggio: il bot userà l'invito generico. */
    private static String threadPost(EventTemplateRequest request) {
        String post = request.discordThreadPost();
        return post == null || post.isBlank() ? null : post;
    }
}
