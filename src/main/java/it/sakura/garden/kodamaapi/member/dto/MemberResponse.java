package it.sakura.garden.kodamaapi.member.dto;

import it.sakura.garden.kodamaapi.member.model.MemberStatus;

import java.time.Instant;

/** Proiezione di un membro esposta dalle API. */
public record MemberResponse(
        Integer id,
        String discordId,
        String username,
        Instant createdAt,
        Instant joinedAt,
        Instant leftAt,
        MemberStatus status,
        String presentation,
        int experience) {
}
