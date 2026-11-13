package it.sakura.garden.kodamaapi.event.dto;

/** Proiezione di un evento esposta dalle API, con il nome della sezione per comodità del bot. */
public record EventResponse(
        Integer id,
        Integer sectionId,
        String sectionName,
        String name,
        String interest,
        int maxParticipants,
        boolean isOpen) {
}
