package it.sakura.garden.kodamaapi.setting.web;

import it.sakura.garden.kodamaapi.auth.model.Role;
import it.sakura.garden.kodamaapi.TestcontainersConfiguration;
import org.springframework.context.annotation.Import;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Verifica il contratto HTTP delle configurazioni, con path su più segmenti. */
@Import(TestcontainersConfiguration.class)
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class SettingControllerIntegrationTest {

    private static final String PATH = "test/limits/max_users";

    @Autowired
    private MockMvc mockMvc;

    private static RequestPostProcessor as(Role role) {
        return jwt().authorities(new SimpleGrantedAuthority(role.authority()));
    }

    @Test
    @DisplayName("un path con più segmenti viene scritto e riletto per intero")
    void readsAndWritesMultiSegmentPath() throws Exception {
        mockMvc.perform(put("/api/v1/settings").param("path", PATH)
                        .with(as(Role.BOT))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"value": "12", "type": "number"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.path").value(PATH))
                .andExpect(jsonPath("$.value").value("12"));

        mockMvc.perform(get("/api/v1/settings").param("path", PATH).with(as(Role.READ_ONLY)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.path").value(PATH))
                .andExpect(jsonPath("$.type").value("number"))
                .andExpect(jsonPath("$.value").value("12"));
    }

    @Test
    @DisplayName("un path inesistente risponde 404")
    void unknownPathReturnsNotFound() throws Exception {
        mockMvc.perform(get("/api/v1/settings").param("path", "test/missing/path").with(as(Role.READ_ONLY)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @DisplayName("senza il parametro path la richiesta risponde 400")
    void missingPathReturnsBadRequest() throws Exception {
        mockMvc.perform(get("/api/v1/settings").with(as(Role.READ_ONLY)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }
}
