package it.sakura.garden.kodamaapi.event.service.impl;

import it.sakura.garden.kodamaapi.event.dto.EventTemplateImage;
import it.sakura.garden.kodamaapi.event.dto.EventTemplateRequest;
import it.sakura.garden.kodamaapi.event.dto.EventTemplateResponse;
import it.sakura.garden.kodamaapi.event.mapper.EventTemplateMapper;
import it.sakura.garden.kodamaapi.event.model.Event;
import it.sakura.garden.kodamaapi.event.model.EventTemplate;
import it.sakura.garden.kodamaapi.event.repository.EventEditionRepository;
import it.sakura.garden.kodamaapi.event.repository.EventRepository;
import it.sakura.garden.kodamaapi.event.repository.EventTemplateRepository;
import it.sakura.garden.kodamaapi.section.model.Section;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/** Verifica la logica di business dei template evento: immagine e messaggio del thread. */
@ExtendWith(MockitoExtension.class)
class EventTemplateServiceImplTest {

    private static final Event EVENT = new Event(new Section("Arte", "arte"), "Arte libera", "123", 12, true);

    @Mock
    private EventTemplateRepository eventTemplateRepository;

    @Mock
    private EventEditionRepository eventEditionRepository;

    @Mock
    private EventRepository eventRepository;

    private EventTemplateServiceImpl eventTemplateService;

    @BeforeEach
    void setUp() {
        eventTemplateService = new EventTemplateServiceImpl(eventTemplateRepository, eventEditionRepository,
                eventRepository, new EventTemplateMapper());
    }

    @Test
    @DisplayName("l'immagine viene decodificata dal data URI con il proprio content type")
    void findImageDecodesDataUri() {
        byte[] bytes = "immagine".getBytes(StandardCharsets.UTF_8);
        String dataUri = "data:image/webp;base64," + Base64.getEncoder().encodeToString(bytes);
        EventTemplate template = new EventTemplate(EVENT, "Titolo", "Descrizione", dataUri, "456", "Notizia", null);
        when(eventTemplateRepository.findById(1)).thenReturn(Optional.of(template));

        EventTemplateImage image = eventTemplateService.findImage(1);

        assertThat(image.contentType()).isEqualTo("image/webp");
        assertThat(image.data()).isEqualTo(bytes);
    }

    @Test
    @DisplayName("un messaggio del thread vuoto viene salvato come assente")
    void blankThreadPostIsStoredAsNull() {
        when(eventRepository.findById(1)).thenReturn(Optional.of(EVENT));
        when(eventTemplateRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        EventTemplateResponse response = eventTemplateService.create(new EventTemplateRequest(
                1, "Titolo", "Descrizione", "data:image/png;base64,AAAA", "456", "Notizia", "   "));

        assertThat(response.discordThreadPost()).isNull();
        assertThat(response.eventName()).isEqualTo("Arte libera");
    }
}
