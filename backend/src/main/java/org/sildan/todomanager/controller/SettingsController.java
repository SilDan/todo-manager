package org.sildan.todomanager.controller;

import org.sildan.todomanager.dto.ThemeSettingsDto;
import org.sildan.todomanager.dto.UpdateThemeSettingsRequest;
import org.sildan.todomanager.repository.AppSettingRepository;
import org.sildan.todomanager.settings.AppSetting;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/settings")
public class SettingsController {

    private static final String THEME_KEY = "theme.mode";

    private final AppSettingRepository repository;

    public SettingsController(AppSettingRepository repository) {
        this.repository = repository;
    }

    @PutMapping("/theme")
    public ThemeSettingsDto updateTheme(@RequestBody UpdateThemeSettingsRequest request) {

        if (request == null || request.themeMode() == null) {
            throw new IllegalArgumentException("themeMode is required");
        }

        String normalized = request.themeMode().trim().toUpperCase();
        if (!normalized.equals("LIGHT") && !normalized.equals("DARK")) {
            throw new IllegalArgumentException("Invalid themeMode: " + request.themeMode());
        }

        AppSetting setting = repository.findById(THEME_KEY)
                .orElse(new AppSetting(THEME_KEY, normalized));

        setting.setValue(normalized);

        repository.save(setting);

        return new ThemeSettingsDto(setting.getValue());
    }
}