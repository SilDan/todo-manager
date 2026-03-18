package org.sildan.todomanager.repository;

import org.sildan.todomanager.settings.AppSetting;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AppSettingRepository extends JpaRepository<AppSetting, String> {
}
