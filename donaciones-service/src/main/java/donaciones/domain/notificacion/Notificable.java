package donaciones.domain.notificacion;

import java.util.List;

public interface Notificable {
    List<Contacto> getContactos();
    Contacto getMedioPredeterminado();

    default Notificacion notificar(String mensaje) {
        Contacto medio = getMedioPredeterminado();
        Notificacion notificacion = new Notificacion(this, mensaje);
        if (medio == null && getContactos() != null && !getContactos().isEmpty()) {
            medio = getContactos().get(0);
        }
        if (medio != null) {
            if (medio.enviar(mensaje)) {
                notificacion.marcarComoCompletada();
            }  else {
                notificacion.marcarComoFallida();
            }
            return notificacion;
        }
        return null;
    }
}
