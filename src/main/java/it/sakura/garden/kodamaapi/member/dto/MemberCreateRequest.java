package it.sakura.garden.kodamaapi.member.dto;

import it.sakura.garden.kodamaapi.member.model.MemberStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.time.Instant;

/**
 * Payload di creazione di un membro.
 *
 * <p>{@code status} ed {@code experience} sono opzionali: se assenti valgono i
 * default previsti dalle specifiche ({@code ACTIVE} e {@code 0}).
 */
public record MemberCreateRequest(

        @NotBlank(message = "discordId è obbligatorio")
        @Size(max = 32, message = "discordId non può superare i 32 caratteri")
        @Pattern(regexp = "\\d+", message = "discordId deve contenere solo cifre")
        String discordId,

        @NotBlank(message = "username è obbligatorio")
        @Size(max = 100, message = "username non può superare i 100 caratteri")
        String username,

        Instant joinedAt,

        Instant leftAt,

        MemberStatus status,

        String presentation,

        @PositiveOrZero(message = "experience non può essere negativa")
        Integer experience) {
}
