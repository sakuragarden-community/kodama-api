package it.sakura.garden.kodamaapi.member.web;

import it.sakura.garden.kodamaapi.auth.model.Role;
import it.sakura.garden.kodamaapi.member.model.Member;
import it.sakura.garden.kodamaapi.member.model.MemberStatus;
import it.sakura.garden.kodamaapi.member.repository.MemberRepository;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Verifica il contratto HTTP dei membri e le regole di autorizzazione.
 *
 * <p>Il token è simulato con {@code jwt()}: qui interessa cosa autorizzano i
 * ruoli, non come vengono emessi — quello è coperto da
 * {@code TokenEndpointIntegrationTest}.
 */
@Import(TestcontainersConfiguration.class)
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class MemberControllerIntegrationTest {

    private static final String DISCORD_ID = "987654321098765432";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MemberRepository memberRepository;

    private static RequestPostProcessor as(Role role) {
        return jwt().authorities(new SimpleGrantedAuthority(role.authority()));
    }

    @Test
    @DisplayName("senza token le API rispondono 401 con corpo JSON")
    void rejectsAnonymousAccess() throws Exception {
        mockMvc.perform(get("/api/v1/members"))
                .andExpect(status().isUnauthorized())
                .andExpect(header().exists("WWW-Authenticate"))
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    @DisplayName("il ruolo READ_ONLY legge ma non scrive")
    void readOnlyRoleCannotWrite() throws Exception {
        mockMvc.perform(get("/api/v1/members").with(as(Role.READ_ONLY)))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/v1/members")
                        .with(as(Role.READ_ONLY))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"discordId": "%s", "username": "kodama"}
                                """.formatted(DISCORD_ID)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    @DisplayName("il ruolo BOT completa il ciclo di vita di un membro")
    void botRoleCompletesCrudCycle() throws Exception {
        mockMvc.perform(post("/api/v1/members")
                        .with(as(Role.BOT))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"discordId": "%s", "username": "kodama"}
                                """.formatted(DISCORD_ID)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", org.hamcrest.Matchers.containsString("/api/v1/members/")))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.experience").value(0));

        Integer id = memberRepository.findByDiscordId(DISCORD_ID).orElseThrow().getId();

        mockMvc.perform(put("/api/v1/members/{id}", id)
                        .with(as(Role.BOT))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username": "kodama-2", "status": "LEFT", "experience": 42}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("kodama-2"))
                .andExpect(jsonPath("$.status").value("LEFT"))
                // Il discordId non è aggiornabile: resta quello di creazione.
                .andExpect(jsonPath("$.discordId").value(DISCORD_ID));

        mockMvc.perform(delete("/api/v1/members/{id}", id).with(as(Role.BOT)))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/members/{id}", id).with(as(Role.BOT)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("un discordId già usato produce 409")
    void rejectsDuplicateDiscordId() throws Exception {
        memberRepository.save(Member.builder()
                .discordId(DISCORD_ID)
                .username("esistente")
                .status(MemberStatus.ACTIVE)
                .build());

        mockMvc.perform(post("/api/v1/members")
                        .with(as(Role.BOT))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"discordId": "%s", "username": "nuovo"}
                                """.formatted(DISCORD_ID)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    @DisplayName("un payload non valido elenca le violazioni per campo")
    void reportsFieldViolations() throws Exception {
        mockMvc.perform(post("/api/v1/members")
                        .with(as(Role.BOT))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"discordId": "non-numerico", "username": "", "experience": -1}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.violations").isArray())
                .andExpect(jsonPath("$.violations.length()").value(3));
    }

    @Test
    @DisplayName("il filtro per status restituisce solo i membri corrispondenti")
    void filtersByStatus() throws Exception {
        memberRepository.save(Member.builder()
                .discordId(DISCORD_ID)
                .username("uscito")
                .status(MemberStatus.LEFT)
                .build());

        mockMvc.perform(get("/api/v1/members")
                        .param("status", MemberStatus.LEFT.name())
                        .with(as(Role.READ_ONLY)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[?(@.discordId == '%s')]".formatted(DISCORD_ID)).exists())
                .andExpect(jsonPath("$.content[?(@.status != 'LEFT')]").doesNotExist());
    }
}
