package it.sakura.garden.kodamaapi.event.web;

import com.jayway.jsonpath.JsonPath;
import it.sakura.garden.kodamaapi.TestcontainersConfiguration;
import it.sakura.garden.kodamaapi.auth.model.Role;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Verifica il contratto HTTP di eventi, template ed edizioni lungo l'intero flusso di pianificazione. */
@Import(TestcontainersConfiguration.class)
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class EventControllerIntegrationTest {

    private static final byte[] IMAGE = {(byte) 0x89, 'P', 'N', 'G'};

    @Autowired
    private MockMvc mockMvc;

    private static RequestPostProcessor as(Role role) {
        return jwt().authorities(new SimpleGrantedAuthority(role.authority()));
    }

    private int create(String path, String body) throws Exception {
        String response = mockMvc.perform(post(path).with(as(Role.BOT))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        return JsonPath.read(response, "$.id");
    }

    private int createTemplate() throws Exception {
        int sectionId = create("/api/v1/sections", """
                {"name": "Arte", "code": "test-arte"}
                """);
        int eventId = create("/api/v1/events", """
                {"sectionId": %d, "name": "Arte libera", "interest": "123456789", "maxParticipants": 12, "isOpen": true}
                """.formatted(sectionId));

        return create("/api/v1/event-templates", """
                {"eventId": %d, "discordEventTitle": "Serata di arte libera",
                 "discordEventDescription": "Fare arte è ancora più bello in compagnia!",
                 "discordEventImage": "data:image/png;base64,%s",
                 "discordNewsChannel": "987654321", "discordNewsContent": "Torna la serata di arte libera!"}
                """.formatted(eventId, Base64.getEncoder().encodeToString(IMAGE)));
    }

    @Test
    @DisplayName("evento, template ed edizione vengono creati e l'edizione si ritrova filtrando per data")
    void plansAnEdition() throws Exception {
        int templateId = createTemplate();
        Instant plannedAt = Instant.now().plus(7, ChronoUnit.DAYS).truncatedTo(ChronoUnit.SECONDS);

        int editionId = create("/api/v1/event-editions", """
                {"eventTemplateId": %d, "plannedAt": "%s"}
                """.formatted(templateId, plannedAt));

        mockMvc.perform(put("/api/v1/event-editions/{id}", editionId).with(as(Role.BOT))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"eventTemplateId": %d, "plannedAt": "%s", "discordEventId": "1122334455"}
                                """.formatted(templateId, plannedAt)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.discordEventId").value("1122334455"));

        mockMvc.perform(get("/api/v1/event-editions").with(as(Role.READ_ONLY))
                        .param("from", plannedAt.minus(1, ChronoUnit.HOURS).toString())
                        .param("to", plannedAt.plus(1, ChronoUnit.HOURS).toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].id").value(editionId))
                .andExpect(jsonPath("$.content[0].eventTemplateId").value(templateId));

        mockMvc.perform(get("/api/v1/event-editions").with(as(Role.READ_ONLY))
                        .param("from", plannedAt.plus(1, ChronoUnit.HOURS).toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(0));
    }

    @Test
    @DisplayName("il template espone l'evento e restituisce l'immagine in binario")
    void templateExposesEventAndImage() throws Exception {
        int templateId = createTemplate();

        mockMvc.perform(get("/api/v1/event-templates/{id}", templateId).with(as(Role.READ_ONLY)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.eventName").value("Arte libera"))
                .andExpect(jsonPath("$.discordThreadPost").doesNotExist())
                .andExpect(jsonPath("$.discordEventImage").doesNotExist());

        mockMvc.perform(get("/api/v1/event-templates/{id}/image", templateId).with(as(Role.READ_ONLY)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.IMAGE_PNG))
                .andExpect(content().bytes(IMAGE));
    }

    @Test
    @DisplayName("un evento con campi non validi risponde 400 con il dettaglio per campo")
    void invalidEventReturnsViolations() throws Exception {
        mockMvc.perform(post("/api/v1/events").with(as(Role.BOT))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"sectionId": 1, "name": "", "interest": "abc", "maxParticipants": -1}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.violations.length()").value(4));
    }

    @Test
    @DisplayName("un'edizione nel passato viene rifiutata")
    void pastEditionIsRejected() throws Exception {
        int templateId = createTemplate();

        mockMvc.perform(post("/api/v1/event-editions").with(as(Role.BOT))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"eventTemplateId": %d, "plannedAt": "2020-01-01T20:00:00Z"}
                                """.formatted(templateId)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.violations[0].field").value("plannedAt"));
    }

    @Test
    @DisplayName("un template con delle edizioni non può essere eliminato")
    void templateInUseCannotBeDeleted() throws Exception {
        int templateId = createTemplate();
        create("/api/v1/event-editions", """
                {"eventTemplateId": %d, "plannedAt": "%s"}
                """.formatted(templateId, Instant.now().plus(1, ChronoUnit.DAYS)));

        mockMvc.perform(delete("/api/v1/event-templates/{id}", templateId).with(as(Role.BOT)))
                .andExpect(status().isConflict());
    }
}
