package it.sakura.garden.kodamaapi.auth.model;

/**
 * Ruoli attribuibili a un {@link User}. Il prefisso {@code ROLE_} non fa parte
 * della costante ma viene aggiunto in {@link #authority()}, così che il
 * database resti leggibile e Spring Security riceva comunque il formato che si
 * aspetta da {@code hasRole(...)}.
 */
public enum Role {

    /** Client macchina (il bot Discord): lettura e scrittura sul dominio. */
    BOT,

    /** Accesso amministrativo completo, incluse le utenze. */
    ADMIN,

    /** Sola lettura, per integrazioni di monitoraggio o dashboard. */
    READ_ONLY;

    public static final String ROLE_PREFIX = "ROLE_";

    /** Nome dell'authority così come atteso da Spring Security. */
    public String authority() {
        return ROLE_PREFIX + name();
    }
}
