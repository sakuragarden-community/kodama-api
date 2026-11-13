package it.sakura.garden.kodamaapi.event.dto;

/** Immagine di un template evento, già decodificata dal data URI. */
public record EventTemplateImage(String contentType, byte[] data) {
}
