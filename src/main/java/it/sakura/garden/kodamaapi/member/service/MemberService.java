package it.sakura.garden.kodamaapi.member.service;

import it.sakura.garden.kodamaapi.common.web.PageResponse;
import it.sakura.garden.kodamaapi.member.dto.MemberCreateRequest;
import it.sakura.garden.kodamaapi.member.dto.MemberResponse;
import it.sakura.garden.kodamaapi.member.dto.MemberUpdateRequest;
import it.sakura.garden.kodamaapi.member.model.MemberStatus;
import org.springframework.data.domain.Pageable;

/**
 * Logica di business dei membri.
 *
 * <p>Il controller dipende da questa astrazione e non dall'implementazione: la
 * regola di unicità del {@code discordId}, i default e la gestione delle
 * transazioni vivono qui, non nel layer web.
 */
public interface MemberService {

    /**
     * @param status filtro opzionale sullo stato; {@code null} per non filtrare
     */
    PageResponse<MemberResponse> findAll(MemberStatus status, Pageable pageable);

    MemberResponse findById(Integer id);

    /** Ricerca per chiave naturale Discord, il percorso usato dal bot. */
    MemberResponse findByDiscordId(String discordId);

    MemberResponse create(MemberCreateRequest request);

    MemberResponse update(Integer id, MemberUpdateRequest request);

    void delete(Integer id);
}
