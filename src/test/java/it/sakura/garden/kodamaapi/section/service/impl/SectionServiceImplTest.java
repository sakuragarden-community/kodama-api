package it.sakura.garden.kodamaapi.section.service.impl;

import it.sakura.garden.kodamaapi.common.exception.DuplicateResourceException;
import it.sakura.garden.kodamaapi.section.dto.SectionRequest;
import it.sakura.garden.kodamaapi.section.dto.SectionResponse;
import it.sakura.garden.kodamaapi.section.mapper.SectionMapper;
import it.sakura.garden.kodamaapi.section.model.Section;
import it.sakura.garden.kodamaapi.section.repository.SectionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Verifica la logica di business delle sezioni, in particolare l'unicità del code. */
@ExtendWith(MockitoExtension.class)
class SectionServiceImplTest {

    @Mock
    private SectionRepository sectionRepository;

    private SectionServiceImpl sectionService;

    @BeforeEach
    void setUp() {
        sectionService = new SectionServiceImpl(sectionRepository, new SectionMapper());
    }

    @Test
    @DisplayName("la creazione rifiuta un code già presente")
    void createRejectsDuplicateCode() {
        when(sectionRepository.existsByCode("giochi")).thenReturn(true);

        assertThatThrownBy(() -> sectionService.create(new SectionRequest("Giochi", "giochi")))
                .isInstanceOf(DuplicateResourceException.class);

        verify(sectionRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    @DisplayName("l'aggiornamento che mantiene il proprio code non viene scambiato per un duplicato")
    void updateAllowsKeepingOwnCode() {
        Section existing = new Section("Giochi", "giochi");
        when(sectionRepository.findById(1)).thenReturn(Optional.of(existing));

        SectionResponse response = sectionService.update(1, new SectionRequest("Videogiochi", "giochi"));

        assertThat(response.name()).isEqualTo("Videogiochi");
        assertThat(response.code()).isEqualTo("giochi");
        // Il controllo di unicità non deve nemmeno partire: il code non cambia.
        verify(sectionRepository, never()).existsByCode(org.mockito.ArgumentMatchers.anyString());
    }

    @Test
    @DisplayName("l'aggiornamento verso un code occupato da un'altra sezione viene rifiutato")
    void updateRejectsCodeTakenByAnotherSection() {
        Section existing = new Section("Giochi", "giochi");
        when(sectionRepository.findById(1)).thenReturn(Optional.of(existing));
        when(sectionRepository.existsByCode("musica")).thenReturn(true);

        assertThatThrownBy(() -> sectionService.update(1, new SectionRequest("Musica", "musica")))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("musica");

        assertThat(existing.getCode()).isEqualTo("giochi");
    }
}
