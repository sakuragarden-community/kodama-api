package it.sakura.garden.kodamaapi.member.dto;

import it.sakura.garden.kodamaapi.member.model.MemberStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.time.Instant;

/**
 * Payload di aggiornamento completo di un membro (semantica {@code PUT}: i
 * campi omessi vengono azzerati).
 *
 * <p>{@code discordId} non compare volutamente: è la chiave naturale assegnata
 * da Discord e non è modificabile tramite API.
 */
public record MemberUpdateRequest(

        @NotBlank(message = "username è obbligatorio")
        @Size(max = 100, message = "username non può superare i 100 caratteri")
        String username,

        Instant joinedAt,

        Instant leftAt,

        @NotNull(message = "status è obbligatorio")
        MemberStatus status,

        String presentation,

        @NotNull(message = "experience è obbligatoria")
        @PositiveOrZero(message = "experience non può essere negativa")
        Integer experience) {
}
