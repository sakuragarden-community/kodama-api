package it.sakura.garden.kodamaapi.member.service.impl;

import it.sakura.garden.kodamaapi.common.exception.DuplicateResourceException;
import it.sakura.garden.kodamaapi.common.exception.ResourceNotFoundException;
import it.sakura.garden.kodamaapi.member.dto.MemberCreateRequest;
import it.sakura.garden.kodamaapi.member.dto.MemberResponse;
import it.sakura.garden.kodamaapi.member.dto.MemberUpdateRequest;
import it.sakura.garden.kodamaapi.member.mapper.MemberMapper;
import it.sakura.garden.kodamaapi.member.model.Member;
import it.sakura.garden.kodamaapi.member.model.MemberStatus;
import it.sakura.garden.kodamaapi.member.repository.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Verifica la logica di business dei membri isolando la persistenza.
 *
 * <p>Il mapper è quello vero e non un mock: fa parte del comportamento che
 * queste asserzioni descrivono (default applicati, campi non aggiornabili).
 */
@ExtendWith(MockitoExtension.class)
class MemberServiceImplTest {

    private static final String DISCORD_ID = "123456789012345678";

    @Mock
    private MemberRepository memberRepository;

    private MemberServiceImpl memberService;

    @BeforeEach
    void setUp() {
        memberService = new MemberServiceImpl(memberRepository, new MemberMapper());
    }

    @Test
    @DisplayName("la creazione applica i default a status ed experience quando omessi")
    void createAppliesDefaults() {
        MemberCreateRequest request =
                new MemberCreateRequest(DISCORD_ID, "kodama", null, null, null, null, null);

        when(memberRepository.existsByDiscordId(DISCORD_ID)).thenReturn(false);
        when(memberRepository.save(any(Member.class))).thenAnswer(invocation -> invocation.getArgument(0));

        MemberResponse response = memberService.create(request);

        assertThat(response.status()).isEqualTo(MemberStatus.ACTIVE);
        assertThat(response.experience()).isZero();
        assertThat(response.discordId()).isEqualTo(DISCORD_ID);
    }

    @Test
    @DisplayName("la creazione rifiuta un discordId già presente senza toccare il repository")
    void createRejectsDuplicateDiscordId() {
        MemberCreateRequest request =
                new MemberCreateRequest(DISCORD_ID, "kodama", null, null, null, null, null);

        when(memberRepository.existsByDiscordId(DISCORD_ID)).thenReturn(true);

        assertThatThrownBy(() -> memberService.create(request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining(DISCORD_ID);

        verify(memberRepository, never()).save(any());
    }

    @Test
    @DisplayName("la ricerca per id inesistente solleva ResourceNotFoundException")
    void findByIdFailsWhenAbsent() {
        when(memberRepository.findById(42)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> memberService.findById(42))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("42");
    }

    @Test
    @DisplayName("l'aggiornamento riscrive i campi modificabili ma non il discordId")
    void updateLeavesDiscordIdUntouched() {
        Member existing = Member.builder()
                .discordId(DISCORD_ID)
                .username("vecchio")
                .status(MemberStatus.ACTIVE)
                .experience(10)
                .build();

        Instant leftAt = Instant.parse("2026-01-01T00:00:00Z");
        MemberUpdateRequest request =
                new MemberUpdateRequest("nuovo", null, leftAt, MemberStatus.LEFT, "ciao", 250);

        when(memberRepository.findById(1)).thenReturn(Optional.of(existing));

        MemberResponse response = memberService.update(1, request);

        assertThat(response.username()).isEqualTo("nuovo");
        assertThat(response.status()).isEqualTo(MemberStatus.LEFT);
        assertThat(response.leftAt()).isEqualTo(leftAt);
        assertThat(response.experience()).isEqualTo(250);
        assertThat(response.discordId()).isEqualTo(DISCORD_ID);
    }

    @Test
    @DisplayName("la cancellazione di un id inesistente non arriva al repository")
    void deleteFailsWhenAbsent() {
        when(memberRepository.findById(7)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> memberService.delete(7))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(memberRepository, never()).delete(any());
    }
}
