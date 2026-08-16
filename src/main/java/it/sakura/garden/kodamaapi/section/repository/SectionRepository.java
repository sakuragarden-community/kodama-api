package it.sakura.garden.kodamaapi.section.repository;

import it.sakura.garden.kodamaapi.section.model.Section;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Accesso alla persistenza per {@link Section}, dedicato a questa sola entità
 * come da separazione dei ruoli fra service e repository.
 */
@Repository
public interface SectionRepository extends JpaRepository<Section, Integer> {

    Optional<Section> findByCode(String code);

    boolean existsByCode(String code);
}
