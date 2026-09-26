package it.sakura.garden.kodamaapi.setting.repository;

import it.sakura.garden.kodamaapi.setting.model.Setting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/** Accesso alla persistenza per {@link Setting}. */
@Repository
public interface SettingRepository extends JpaRepository<Setting, Integer> {

    Optional<Setting> findByPath(String path);
}
