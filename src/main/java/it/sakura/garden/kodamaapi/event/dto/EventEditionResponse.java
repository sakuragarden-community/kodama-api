package it.sakura.garden.kodamaapi.event.dto;

import java.time.Instant;

/** Proiezione di un'edizione evento esposta dalle API. */
public record EventEditionResponse(
        Integer id,
        Integer eventTemplateId,
        Instant plannedAt,
        String discordEventId) {
}
