package it.sakura.garden.kodamaapi.member.service.impl;

import it.sakura.garden.kodamaapi.common.exception.DuplicateResourceException;
import it.sakura.garden.kodamaapi.common.exception.ResourceNotFoundException;
import it.sakura.garden.kodamaapi.common.web.PageResponse;
import it.sakura.garden.kodamaapi.member.dto.MemberCreateRequest;
import it.sakura.garden.kodamaapi.member.dto.MemberResponse;
import it.sakura.garden.kodamaapi.member.dto.MemberUpdateRequest;
import it.sakura.garden.kodamaapi.member.mapper.MemberMapper;
import it.sakura.garden.kodamaapi.member.model.Member;
import it.sakura.garden.kodamaapi.member.model.MemberStatus;
import it.sakura.garden.kodamaapi.member.repository.MemberRepository;
import it.sakura.garden.kodamaapi.member.service.MemberService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementazione di {@link MemberService}.
 *
 * <p>Ogni accesso ai dati è delegato a {@link MemberRepository}; le dipendenze
 * arrivano dal costruttore, così che la classe resti istanziabile e testabile
 * anche fuori dal contesto Spring.
 */
@Service
@Transactional(readOnly = true)
public class MemberServiceImpl implements MemberService {

    private static final String RESOURCE = "Member";

    private static final Logger log = LoggerFactory.getLogger(MemberServiceImpl.class);

    private final MemberRepository memberRepository;
    private final MemberMapper memberMapper;

    public MemberServiceImpl(MemberRepository memberRepository, MemberMapper memberMapper) {
        this.memberRepository = memberRepository;
        this.memberMapper = memberMapper;
    }

    @Override
    public PageResponse<MemberResponse> findAll(MemberStatus status, Pageable pageable) {
        Page<Member> page = status == null
                ? memberRepository.findAll(pageable)
                : memberRepository.findByStatus(status, pageable);

        return PageResponse.from(page, memberMapper::toResponse);
    }

    @Override
    public MemberResponse findById(Integer id) {
        return memberMapper.toResponse(requireById(id));
    }

    @Override
    public MemberResponse findByDiscordId(String discordId) {
        Member member = memberRepository.findByDiscordId(discordId)
                .orElseThrow(() -> ResourceNotFoundException.of(RESOURCE, "discordId", discordId));

        return memberMapper.toResponse(member);
    }

    @Override
    @Transactional
    public MemberResponse create(MemberCreateRequest request) {
        if (memberRepository.existsByDiscordId(request.discordId())) {
            throw DuplicateResourceException.of(RESOURCE, "discordId", request.discordId());
        }

        Member saved = memberRepository.save(memberMapper.toEntity(request));
        log.info("Creato member id={} discordId={}", saved.getId(), saved.getDiscordId());

        return memberMapper.toResponse(saved);
    }

    /**
     * L'entità è gestita dal persistence context per tutta la transazione:
     * il flush automatico rende superflua una {@code save()} esplicita.
     */
    @Override
    @Transactional
    public MemberResponse update(Integer id, MemberUpdateRequest request) {
        Member member = requireById(id);
        memberMapper.merge(member, request);
        log.info("Aggiornato member id={}", id);

        return memberMapper.toResponse(member);
    }

    @Override
    @Transactional
    public void delete(Integer id) {
        memberRepository.delete(requireById(id));
        log.info("Eliminato member id={}", id);
    }

    private Member requireById(Integer id) {
        return memberRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of(RESOURCE, "id", id));
    }
}
