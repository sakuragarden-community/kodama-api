package it.sakura.garden.kodamaapi.section.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Payload di creazione e aggiornamento di una sezione.
 *
 * <p>Creazione e {@code PUT} condividono lo stesso record perché i campi
 * modificabili coincidono: sdoppiarlo aggiungerebbe un tipo senza aggiungere
 * un vincolo.
 */
public record SectionRequest(

        @NotBlank(message = "name è obbligatorio")
        @Size(max = 100, message = "name non può superare i 100 caratteri")
        String name,

        @NotBlank(message = "code è obbligatorio")
        @Size(max = 50, message = "code non può superare i 50 caratteri")
        @Pattern(regexp = "[a-z0-9-]+",
                message = "code può contenere solo lettere minuscole, cifre e trattini")
        String code) {
}
