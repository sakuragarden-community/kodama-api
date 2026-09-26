package it.sakura.garden.kodamaapi.setting.web;

import it.sakura.garden.kodamaapi.setting.dto.SettingRequest;
import it.sakura.garden.kodamaapi.setting.dto.SettingResponse;
import it.sakura.garden.kodamaapi.setting.service.SettingService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * API di lettura e scrittura delle configurazioni, indirizzate per path
 * (es. {@code /api/v1/settings/discord.channels.welcome}).
 */
@RestController
@RequestMapping("/api/v1/settings")
public class SettingController {

    private final SettingService settingService;

    public SettingController(SettingService settingService) {
        this.settingService = settingService;
    }

    @GetMapping("/{path}")
    public SettingResponse get(@PathVariable String path) {
        return settingService.getSetting(path);
    }

    /** Upsert: crea la configurazione se il path non esiste ancora. */
    @PutMapping("/{path}")
    @PreAuthorize("hasAnyRole('BOT', 'ADMIN')")
    public SettingResponse set(@PathVariable String path, @Valid @RequestBody SettingRequest request) {
        return settingService.setSetting(path, request.value(), request.type());
    }
}
