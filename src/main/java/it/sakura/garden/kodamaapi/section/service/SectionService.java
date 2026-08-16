package it.sakura.garden.kodamaapi.section.service;

import it.sakura.garden.kodamaapi.common.web.PageResponse;
import it.sakura.garden.kodamaapi.section.dto.SectionRequest;
import it.sakura.garden.kodamaapi.section.dto.SectionResponse;
import org.springframework.data.domain.Pageable;

/**
 * Logica di business delle sezioni, inclusa la tutela dell'unicità del
 * {@code code} in creazione e in aggiornamento.
 */
public interface SectionService {

    PageResponse<SectionResponse> findAll(Pageable pageable);

    SectionResponse findById(Integer id);

    /** Ricerca per chiave naturale, il percorso usato dal bot. */
    SectionResponse findByCode(String code);

    SectionResponse create(SectionRequest request);

    SectionResponse update(Integer id, SectionRequest request);

    void delete(Integer id);
}
