package it.sakura.garden.kodamaapi.setting.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Payload di scrittura di una configurazione. Il path non compare perché
 * arriva dall'URL.
 *
 * @param value nuovo valore, {@code null} per svuotarlo
 * @param type  opzionale: se omesso resta quello attuale, o {@code text} per
 *              una configurazione nuova
 */
public record SettingRequest(

        String value,

        @Size(max = 32, message = "type non può superare i 32 caratteri")
        @Pattern(regexp = "[a-z0-9_-]+",
                message = "type può contenere solo lettere minuscole, cifre, trattini e underscore")
        String type) {
}
