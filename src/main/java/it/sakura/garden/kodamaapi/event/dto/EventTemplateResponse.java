package it.sakura.garden.kodamaapi.event.dto;

/**
 * Proiezione di un template evento esposta dalle API.
 *
 * <p>L'immagine non è inclusa per non appesantire le liste: si scarica da
 * {@code GET /event-templates/{id}/image}.
 */
public record EventTemplateResponse(
        Integer id,
        Integer eventId,
        String eventName,
        String name,
        String discordEventTitle,
        String discordEventDescription,
        String discordNewsChannel,
        String discordNewsContent,
        String discordThreadPost) {
}
