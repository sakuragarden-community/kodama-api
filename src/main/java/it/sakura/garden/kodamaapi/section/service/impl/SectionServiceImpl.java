package it.sakura.garden.kodamaapi.section.service.impl;

import it.sakura.garden.kodamaapi.common.exception.DuplicateResourceException;
import it.sakura.garden.kodamaapi.common.exception.ResourceNotFoundException;
import it.sakura.garden.kodamaapi.common.web.PageResponse;
import it.sakura.garden.kodamaapi.section.dto.SectionRequest;
import it.sakura.garden.kodamaapi.section.dto.SectionResponse;
import it.sakura.garden.kodamaapi.section.mapper.SectionMapper;
import it.sakura.garden.kodamaapi.section.model.Section;
import it.sakura.garden.kodamaapi.section.repository.SectionRepository;
import it.sakura.garden.kodamaapi.section.service.SectionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Implementazione di {@link SectionService}, con accesso ai dati delegato al repository. */
@Service
@Transactional(readOnly = true)
public class SectionServiceImpl implements SectionService {

    private static final String RESOURCE = "Section";

    private static final Logger log = LoggerFactory.getLogger(SectionServiceImpl.class);

    private final SectionRepository sectionRepository;
    private final SectionMapper sectionMapper;

    public SectionServiceImpl(SectionRepository sectionRepository, SectionMapper sectionMapper) {
        this.sectionRepository = sectionRepository;
        this.sectionMapper = sectionMapper;
    }

    @Override
    public PageResponse<SectionResponse> findAll(Pageable pageable) {
        return PageResponse.from(sectionRepository.findAll(pageable), sectionMapper::toResponse);
    }

    @Override
    public SectionResponse findById(Integer id) {
        return sectionMapper.toResponse(requireById(id));
    }

    @Override
    public SectionResponse findByCode(String code) {
        Section section = sectionRepository.findByCode(code)
                .orElseThrow(() -> ResourceNotFoundException.of(RESOURCE, "code", code));

        return sectionMapper.toResponse(section);
    }

    @Override
    @Transactional
    public SectionResponse create(SectionRequest request) {
        if (sectionRepository.existsByCode(request.code())) {
            throw DuplicateResourceException.of(RESOURCE, "code", request.code());
        }

        Section saved = sectionRepository.save(sectionMapper.toEntity(request));
        log.info("Creata section id={} code={}", saved.getId(), saved.getCode());

        return sectionMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public SectionResponse update(Integer id, SectionRequest request) {
        Section section = requireById(id);

        // Il code è modificabile, ma solo verso un valore non già occupato da
        // un'altra sezione: il confronto evita il falso positivo su se stessa.
        if (!section.getCode().equals(request.code()) && sectionRepository.existsByCode(request.code())) {
            throw DuplicateResourceException.of(RESOURCE, "code", request.code());
        }

        sectionMapper.merge(section, request);
        log.info("Aggiornata section id={}", id);

        return sectionMapper.toResponse(section);
    }

    @Override
    @Transactional
    public void delete(Integer id) {
        sectionRepository.delete(requireById(id));
        log.info("Eliminata section id={}", id);
    }

    private Section requireById(Integer id) {
        return sectionRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of(RESOURCE, "id", id));
    }
}
