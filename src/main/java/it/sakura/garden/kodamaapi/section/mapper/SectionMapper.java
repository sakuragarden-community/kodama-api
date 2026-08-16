package it.sakura.garden.kodamaapi.section.mapper;

import it.sakura.garden.kodamaapi.common.mapper.EntityMapper;
import it.sakura.garden.kodamaapi.section.dto.SectionRequest;
import it.sakura.garden.kodamaapi.section.dto.SectionResponse;
import it.sakura.garden.kodamaapi.section.model.Section;
import org.springframework.stereotype.Component;

/** Factory e proiettore per {@link Section}. */
@Component
public class SectionMapper implements EntityMapper<Section, SectionRequest, SectionRequest, SectionResponse> {

    @Override
    public Section toEntity(SectionRequest request) {
        return new Section(request.name(), request.code());
    }

    @Override
    public void merge(Section section, SectionRequest request) {
        section.setName(request.name());
        section.setCode(request.code());
    }

    @Override
    public SectionResponse toResponse(Section section) {
        return new SectionResponse(section.getId(), section.getName(), section.getCode());
    }
}
