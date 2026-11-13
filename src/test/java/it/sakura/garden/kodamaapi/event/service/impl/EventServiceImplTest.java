package it.sakura.garden.kodamaapi.event.service.impl;

import it.sakura.garden.kodamaapi.common.exception.ResourceInUseException;
import it.sakura.garden.kodamaapi.common.exception.ResourceNotFoundException;
import it.sakura.garden.kodamaapi.event.dto.EventRequest;
import it.sakura.garden.kodamaapi.event.dto.EventResponse;
import it.sakura.garden.kodamaapi.event.mapper.EventMapper;
import it.sakura.garden.kodamaapi.event.model.Event;
import it.sakura.garden.kodamaapi.event.repository.EventRepository;
import it.sakura.garden.kodamaapi.event.repository.EventTemplateRepository;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Verifica la logica di business degli eventi: sezione referenziata, default e cancellazione. */
@ExtendWith(MockitoExtension.class)
class EventServiceImplTest {

    @Mock
    private EventRepository eventRepository;

    @Mock
    private EventTemplateRepository eventTemplateRepository;

    @Mock
    private SectionRepository sectionRepository;

    private EventServiceImpl eventService;

    @BeforeEach
    void setUp() {
        eventService = new EventServiceImpl(eventRepository, eventTemplateRepository, sectionRepository,
                new EventMapper());
    }

    @Test
    @DisplayName("la creazione rifiuta una sezione inesistente")
    void createRejectsUnknownSection() {
        when(sectionRepository.findById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> eventService.create(new EventRequest(99, "Arte libera", "123", 0, true)))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Section");

        verify(eventRepository, never()).save(any());
    }

    @Test
    @DisplayName("un numero di partecipanti omesso equivale a nessun limite")
    void missingMaxParticipantsMeansNoLimit() {
        when(sectionRepository.findById(1)).thenReturn(Optional.of(new Section("Arte", "arte")));
        when(eventRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        EventResponse response = eventService.create(new EventRequest(1, "Arte libera", "123", null, false));

        assertThat(response.maxParticipants()).isZero();
        assertThat(response.isOpen()).isFalse();
        assertThat(response.sectionName()).isEqualTo("Arte");
    }

    @Test
    @DisplayName("un evento con dei template non può essere eliminato")
    void deleteRejectsEventWithTemplates() {
        Event event = new Event(new Section("Arte", "arte"), "Arte libera", "123", 0, true);
        when(eventRepository.findById(1)).thenReturn(Optional.of(event));
        when(eventTemplateRepository.existsByEventId(1)).thenReturn(true);

        assertThatThrownBy(() -> eventService.delete(1))
                .isInstanceOf(ResourceInUseException.class)
                .hasMessageContaining("EventTemplate");

        verify(eventRepository, never()).delete(any());
    }
}
