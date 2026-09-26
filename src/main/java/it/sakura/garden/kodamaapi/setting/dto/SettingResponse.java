package it.sakura.garden.kodamaapi.setting.dto;

/** Proiezione di una configurazione esposta dalle API. */
public record SettingResponse(String path, String type, String value) {
}
