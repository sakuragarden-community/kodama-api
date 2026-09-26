package it.sakura.garden.kodamaapi.setting.service.impl;

import it.sakura.garden.kodamaapi.common.exception.ResourceNotFoundException;
import it.sakura.garden.kodamaapi.setting.dto.SettingResponse;
import it.sakura.garden.kodamaapi.setting.mapper.SettingMapper;
import it.sakura.garden.kodamaapi.setting.model.Setting;
import it.sakura.garden.kodamaapi.setting.repository.SettingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Verifica lettura e upsert delle configurazioni. */
@ExtendWith(MockitoExtension.class)
class SettingServiceImplTest {

    @Mock
    private SettingRepository settingRepository;

    private SettingServiceImpl settingService;

    @BeforeEach
    void setUp() {
        settingService = new SettingServiceImpl(settingRepository, new SettingMapper());
    }

    @Test
    @DisplayName("la lettura di un path inesistente solleva ResourceNotFoundException")
    void getSettingRejectsUnknownPath() {
        when(settingRepository.findByPath("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> settingService.getSetting("missing"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("missing");
    }

    @Test
    @DisplayName("la scrittura su un path nuovo crea la configurazione con tipo text")
    void setSettingCreatesWithDefaultType() {
        when(settingRepository.findByPath("bot.prefix")).thenReturn(Optional.empty());
        when(settingRepository.save(any(Setting.class))).thenAnswer(invocation -> invocation.getArgument(0));

        SettingResponse response = settingService.setSetting("bot.prefix", "!");

        assertThat(response).isEqualTo(new SettingResponse("bot.prefix", "text", "!"));
    }

    @Test
    @DisplayName("la scrittura su un path esistente aggiorna il valore e conserva il tipo se non indicato")
    void setSettingUpdatesExistingKeepingType() {
        Setting existing = new Setting("xp.multiplier", "number", "1");
        when(settingRepository.findByPath("xp.multiplier")).thenReturn(Optional.of(existing));

        SettingResponse response = settingService.setSetting("xp.multiplier", "2", null);

        assertThat(response).isEqualTo(new SettingResponse("xp.multiplier", "number", "2"));
        verify(settingRepository, never()).save(any());
    }
}
