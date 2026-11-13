package it.sakura.garden.kodamaapi.event.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Payload di creazione e aggiornamento di un template evento.
 *
 * <p>I limiti di lunghezza seguono quelli di Discord: nome del template max
 * 100 come l'etichetta di un'opzione delle select, titolo dell'evento max
 * 100, descrizione max 1000 di cui {@value #DESCRIPTION_MAX_LENGTH} riservati al
 * testo libero (il resto è la riga di riepilogo composta dal bot), messaggi max
 * 2000 meno lo spazio per il link all'evento in coda alla notizia.
 *
 * @param discordEventImage immagine come data URI base64
 *                          ({@code data:image/png;base64,...}), max
 *                          {@value #IMAGE_MAX_BYTES} byte decodificati
 * @param discordThreadPost opzionale: senza, il thread si apre con un invito generico a commentare
 */
public record EventTemplateRequest(

        @NotNull(message = "eventId è obbligatorio")
        Integer eventId,

        @NotBlank(message = "name è obbligatorio")
        @Size(max = 100, message = "name non può superare i 100 caratteri")
        String name,

        @NotBlank(message = "discordEventTitle è obbligatorio")
        @Size(max = 100, message = "discordEventTitle non può superare i 100 caratteri")
        String discordEventTitle,

        @NotBlank(message = "discordEventDescription è obbligatorio")
        @Size(max = DESCRIPTION_MAX_LENGTH,
                message = "discordEventDescription non può superare i " + DESCRIPTION_MAX_LENGTH + " caratteri")
        String discordEventDescription,

        @NotBlank(message = "discordEventImage è obbligatorio")
        @Size(max = IMAGE_MAX_LENGTH, message = "discordEventImage non può superare i 4 MB")
        @Pattern(regexp = "data:image/(png|jpeg|gif|webp);base64,[A-Za-z0-9+/]+={0,2}",
                message = "discordEventImage deve essere un'immagine PNG, JPEG, GIF o WEBP codificata come data URI base64")
        String discordEventImage,

        @NotBlank(message = "discordNewsChannel è obbligatorio")
        @Size(max = 32, message = "discordNewsChannel non può superare i 32 caratteri")
        @Pattern(regexp = "\\d+", message = "discordNewsChannel deve essere l'ID di un canale Discord (solo cifre)")
        String discordNewsChannel,

        @NotBlank(message = "discordNewsContent è obbligatorio")
        @Size(max = NEWS_MAX_LENGTH,
                message = "discordNewsContent non può superare i " + NEWS_MAX_LENGTH + " caratteri")
        String discordNewsContent,

        @Size(max = THREAD_POST_MAX_LENGTH,
                message = "discordThreadPost non può superare i " + THREAD_POST_MAX_LENGTH + " caratteri")
        String discordThreadPost) {

    public static final int DESCRIPTION_MAX_LENGTH = 800;
    public static final int NEWS_MAX_LENGTH = 1800;
    public static final int THREAD_POST_MAX_LENGTH = 2000;
    public static final int IMAGE_MAX_BYTES = 4 * 1024 * 1024;
    /** Lunghezza del data URI di un'immagine di {@link #IMAGE_MAX_BYTES} byte, prefisso incluso. */
    public static final int IMAGE_MAX_LENGTH = (IMAGE_MAX_BYTES + 2) / 3 * 4 + 32;
}
