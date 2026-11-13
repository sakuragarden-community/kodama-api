package it.sakura.garden.kodamaapi.event.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

/**
 * Payload di creazione e aggiornamento di un evento.
 *
 * @param interest        ID del ruolo Discord di riferimento
 * @param maxParticipants numero massimo di partecipanti, {@code 0} (default) per nessun limite
 * @param isOpen          {@code false} se la partecipazione richiede un'iscrizione
 */
public record EventRequest(

        @NotNull(message = "sectionId è obbligatorio")
        Integer sectionId,

        @NotBlank(message = "name è obbligatorio")
        @Size(max = 100, message = "name non può superare i 100 caratteri")
        String name,

        @NotBlank(message = "interest è obbligatorio")
        @Size(max = 32, message = "interest non può superare i 32 caratteri")
        @Pattern(regexp = "\\d+", message = "interest deve essere l'ID di un ruolo Discord (solo cifre)")
        String interest,

        @PositiveOrZero(message = "maxParticipants non può essere negativo")
        Integer maxParticipants,

        @NotNull(message = "isOpen è obbligatorio")
        Boolean isOpen) {
}
