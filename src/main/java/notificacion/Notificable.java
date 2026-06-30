package notificacion;

import donante.Contacto;
import java.util.List;

public interface Notificable {
    List<Contacto> getContactos();
    Contacto getMedioPredeterminado();

    default Notificacion notificar(String mensaje) {
        Contacto medio = getMedioPredeterminado();
        if (medio == null && getContactos() != null && !getContactos().isEmpty()) {
            medio = getContactos().get(0);
        }
        if (medio != null) {
            Notificacion notificacion = new Notificacion(medio, mensaje);
            notificacion.enviar();
            return notificacion;
        }
        return null;
    }
}
