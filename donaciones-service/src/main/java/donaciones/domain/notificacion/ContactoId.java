package donaciones.domain.notificacion;

import java.io.Serializable;
import java.util.Objects;

/** Clave compuesta definida por el DER: notificable_id + tipo_notificable. */
public class ContactoId implements Serializable {
    private Long notificableId;
    private String tipoNotificable;

    public ContactoId() { }
    public ContactoId(Long notificableId, String tipoNotificable) {
        this.notificableId = notificableId;
        this.tipoNotificable = tipoNotificable;
    }
    @Override public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof ContactoId that)) return false;
        return Objects.equals(notificableId, that.notificableId) && Objects.equals(tipoNotificable, that.tipoNotificable);
    }
    @Override public int hashCode() { return Objects.hash(notificableId, tipoNotificable); }
}
