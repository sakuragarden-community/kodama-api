package it.sakura.garden.kodamaapi.member.mapper;

import it.sakura.garden.kodamaapi.common.mapper.EntityMapper;
import it.sakura.garden.kodamaapi.member.dto.MemberCreateRequest;
import it.sakura.garden.kodamaapi.member.dto.MemberResponse;
import it.sakura.garden.kodamaapi.member.dto.MemberUpdateRequest;
import it.sakura.garden.kodamaapi.member.model.Member;
import it.sakura.garden.kodamaapi.member.model.MemberStatus;
import org.springframework.stereotype.Component;

/**
 * Factory e proiettore per {@link Member}: unico punto in cui il progetto sa
 * come si passa dai DTO all'entità e viceversa.
 */
@Component
public class MemberMapper implements EntityMapper<Member, MemberCreateRequest, MemberUpdateRequest, MemberResponse> {

    /** Valori di default previsti dalle specifiche quando il client li omette. */
    private static final MemberStatus DEFAULT_STATUS = MemberStatus.ACTIVE;
    private static final int DEFAULT_EXPERIENCE = 0;

    @Override
    public Member toEntity(MemberCreateRequest request) {
        return Member.builder()
                .discordId(request.discordId())
                .username(request.username())
                .joinedAt(request.joinedAt())
                .leftAt(request.leftAt())
                .status(request.status() != null ? request.status() : DEFAULT_STATUS)
                .presentation(request.presentation())
                .experience(request.experience() != null ? request.experience() : DEFAULT_EXPERIENCE)
                .build();
    }

    /**
     * Semantica {@code PUT}: ogni campo modificabile viene riscritto con quanto
     * arriva dalla richiesta. {@code discordId} e {@code createdAt} restano
     * immutabili per costruzione, non essendo esposti da {@link MemberUpdateRequest}.
     */
    @Override
    public void merge(Member member, MemberUpdateRequest request) {
        member.setUsername(request.username());
        member.setJoinedAt(request.joinedAt());
        member.setLeftAt(request.leftAt());
        member.setStatus(request.status());
        member.setPresentation(request.presentation());
        member.setExperience(request.experience());
    }

    @Override
    public MemberResponse toResponse(Member member) {
        return new MemberResponse(
                member.getId(),
                member.getDiscordId(),
                member.getUsername(),
                member.getCreatedAt(),
                member.getJoinedAt(),
                member.getLeftAt(),
                member.getStatus(),
                member.getPresentation(),
                member.getExperience());
    }
}
