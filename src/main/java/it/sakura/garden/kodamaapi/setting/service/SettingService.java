package it.sakura.garden.kodamaapi.setting.service;

import it.sakura.garden.kodamaapi.setting.dto.SettingResponse;

/** Lettura e scrittura delle configurazioni applicative, indirizzate per path. */
public interface SettingService {

    /** @throws it.sakura.garden.kodamaapi.common.exception.ResourceNotFoundException se il path non esiste */
    SettingResponse getSetting(String path);

    /** Imposta il valore al path, creando la configurazione se non esiste (tipo {@code text}). */
    SettingResponse setSetting(String path, String value);

    /**
     * Come {@link #setSetting(String, String)}, ma aggiorna anche il tipo;
     * un {@code type} nullo lascia invariato quello attuale.
     */
    SettingResponse setSetting(String path, String value, String type);
}
