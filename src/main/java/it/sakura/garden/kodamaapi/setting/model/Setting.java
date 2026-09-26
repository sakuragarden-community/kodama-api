package it.sakura.garden.kodamaapi.setting.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.Objects;

/**
 * Configurazione applicativa identificata univocamente dal proprio
 * {@code path}. Il valore è sempre memorizzato come testo: {@code type} indica
 * a chi lo legge come interpretarlo.
 */
@Entity
@Table(name = "settings")
public class Setting {

    /** Tipo assegnato quando la configurazione viene creata senza specificarlo. */
    public static final String DEFAULT_TYPE = "text";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "path", nullable = false, unique = true, length = 255)
    private String path;

    @Column(name = "type", nullable = false, length = 32)
    private String type;

    @Column(name = "value", columnDefinition = "text")
    private String value;

    protected Setting() {
        // richiesto da JPA
    }

    public Setting(String path, String type, String value) {
        this.path = path;
        this.type = type != null ? type : DEFAULT_TYPE;
        this.value = value;
    }

    public Integer getId() {
        return id;
    }

    public String getPath() {
        return path;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        return other instanceof Setting setting && Objects.equals(path, setting.path);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(path);
    }

    @Override
    public String toString() {
        return "Setting{id=%d, path='%s', type='%s'}".formatted(id, path, type);
    }
}
