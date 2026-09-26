package it.sakura.garden.kodamaapi.setting.mapper;

import it.sakura.garden.kodamaapi.setting.dto.SettingResponse;
import it.sakura.garden.kodamaapi.setting.model.Setting;
import org.springframework.stereotype.Component;

/**
 * Proiettore per {@link Setting}. Non implementa {@code EntityMapper}: la
 * scrittura è un upsert per path, non una coppia creazione/aggiornamento.
 */
@Component
public class SettingMapper {

    public SettingResponse toResponse(Setting setting) {
        return new SettingResponse(setting.getPath(), setting.getType(), setting.getValue());
    }
}
