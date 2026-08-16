package it.sakura.garden.kodamaapi.member.repository;

import it.sakura.garden.kodamaapi.member.model.Member;
import it.sakura.garden.kodamaapi.member.model.MemberStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Accesso alla persistenza per {@link Member}. È l'unico punto del sistema
 * autorizzato a parlare con il database per questa entità: il service layer vi
 * delega ogni operazione di CRUD.
 */
@Repository
public interface MemberRepository extends JpaRepository<Member, Integer> {

    Optional<Member> findByDiscordId(String discordId);

    boolean existsByDiscordId(String discordId);

    Page<Member> findByStatus(MemberStatus status, Pageable pageable);
}
