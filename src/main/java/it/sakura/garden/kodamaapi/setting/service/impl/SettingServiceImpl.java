package it.sakura.garden.kodamaapi.setting.service.impl;

import it.sakura.garden.kodamaapi.common.exception.ResourceNotFoundException;
import it.sakura.garden.kodamaapi.setting.dto.SettingResponse;
import it.sakura.garden.kodamaapi.setting.mapper.SettingMapper;
import it.sakura.garden.kodamaapi.setting.model.Setting;
import it.sakura.garden.kodamaapi.setting.repository.SettingRepository;
import it.sakura.garden.kodamaapi.setting.service.SettingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Implementazione di {@link SettingService}, con accesso ai dati delegato al repository. */
@Service
@Transactional(readOnly = true)
public class SettingServiceImpl implements SettingService {

    private static final String RESOURCE = "Setting";

    private static final Logger log = LoggerFactory.getLogger(SettingServiceImpl.class);

    private final SettingRepository settingRepository;
    private final SettingMapper settingMapper;

    public SettingServiceImpl(SettingRepository settingRepository, SettingMapper settingMapper) {
        this.settingRepository = settingRepository;
        this.settingMapper = settingMapper;
    }

    @Override
    public SettingResponse getSetting(String path) {
        Setting setting = settingRepository.findByPath(path)
                .orElseThrow(() -> ResourceNotFoundException.of(RESOURCE, "path", path));

        return settingMapper.toResponse(setting);
    }

    @Override
    @Transactional
    public SettingResponse setSetting(String path, String value) {
        return setSetting(path, value, null);
    }

    @Override
    @Transactional
    public SettingResponse setSetting(String path, String value, String type) {
        Setting setting = settingRepository.findByPath(path).orElse(null);

        if (setting == null) {
            setting = settingRepository.save(new Setting(path, type, value));
            log.info("Creato setting path={} type={}", path, setting.getType());
        } else {
            setting.setValue(value);
            if (type != null) {
                setting.setType(type);
            }
            log.info("Aggiornato setting path={}", path);
        }

        return settingMapper.toResponse(setting);
    }
}
