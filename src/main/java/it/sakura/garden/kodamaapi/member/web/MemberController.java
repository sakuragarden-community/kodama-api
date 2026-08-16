package it.sakura.garden.kodamaapi.member.web;

import it.sakura.garden.kodamaapi.common.web.PageResponse;
import it.sakura.garden.kodamaapi.member.dto.MemberCreateRequest;
import it.sakura.garden.kodamaapi.member.dto.MemberResponse;
import it.sakura.garden.kodamaapi.member.dto.MemberUpdateRequest;
import it.sakura.garden.kodamaapi.member.model.MemberStatus;
import it.sakura.garden.kodamaapi.member.service.MemberService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * API CRUD sui membri.
 *
 * <p>Il controller si limita a tradurre HTTP ↔ DTO e a delegare a
 * {@link MemberService}: nessuna regola di business e nessun accesso al
 * repository transitano da qui.
 */
@RestController
@RequestMapping("/api/v1/members")
public class MemberController {

    private final MemberService memberService;

    public MemberController(MemberService memberService) {
        this.memberService = memberService;
    }

    @GetMapping
    public PageResponse<MemberResponse> list(
            @RequestParam(required = false) MemberStatus status,
            @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.ASC) Pageable pageable) {

        return memberService.findAll(status, pageable);
    }

    @GetMapping("/{id}")
    public MemberResponse getById(@PathVariable Integer id) {
        return memberService.findById(id);
    }

    /** Lookup per snowflake Discord: è il modo in cui il bot risolve un membro. */
    @GetMapping("/by-discord-id/{discordId}")
    public MemberResponse getByDiscordId(@PathVariable String discordId) {
        return memberService.findByDiscordId(discordId);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('BOT', 'ADMIN')")
    public ResponseEntity<MemberResponse> create(@Valid @RequestBody MemberCreateRequest request,
                                                 UriComponentsBuilder uriBuilder) {
        MemberResponse created = memberService.create(request);

        return ResponseEntity
                .created(uriBuilder.path("/api/v1/members/{id}").buildAndExpand(created.id()).toUri())
                .body(created);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('BOT', 'ADMIN')")
    public MemberResponse update(@PathVariable Integer id, @Valid @RequestBody MemberUpdateRequest request) {
        return memberService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('BOT', 'ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        memberService.delete(id);

        return ResponseEntity.noContent().build();
    }
}
