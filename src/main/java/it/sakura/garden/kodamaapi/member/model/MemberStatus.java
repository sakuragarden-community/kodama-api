package it.sakura.garden.kodamaapi.member.model;

/**
 * Stato del ciclo di vita di un {@link Member} all'interno del server Discord.
 * Persistito come stringa per restare leggibile a database e stabile rispetto
 * all'ordinamento delle costanti.
 */
public enum MemberStatus {

    /** Membro attualmente presente e attivo nel server. */
    ACTIVE,

    /** Membro ancora presente ma inattivo da tempo. */
    INACTIVE,

    /** Membro che ha lasciato il server: {@code leftAt} è valorizzato. */
    LEFT,

    /** Membro espulso o bannato dal server. */
    BANNED
}
