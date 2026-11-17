package it.sakura.garden.kodamaapi.event.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.Instant;

/**
 * Payload di creazione e aggiornamento di un'edizione evento.
 *
 * @param discordEventId evento programmato creato dal bot su Discord; opzionale,
 *                       il bot lo valorizza dopo averlo creato
 * @param discordNewsThreadId thread della notizia della settimana prima; opzionale,
 *                            il bot lo valorizza dopo averla pubblicata
 */
public record EventEditionRequest(

        @NotNull(message = "eventTemplateId è obbligatorio")
        Integer eventTemplateId,

        @NotNull(message = "plannedAt è obbligatorio")
        @Future(message = "plannedAt deve essere una data futura")
        Instant plannedAt,

        @Size(max = 32, message = "discordEventId non può superare i 32 caratteri")
        @Pattern(regexp = "\\d+", message = "discordEventId deve contenere solo cifre")
        String discordEventId,

        @Size(max = 32, message = "discordNewsThreadId non può superare i 32 caratteri")
        @Pattern(regexp = "\\d+", message = "discordNewsThreadId deve contenere solo cifre")
        String discordNewsThreadId) {
}
