package notificacion;

import donante.Contacto;
import donante.TipoContacto;

public class ServicioDeNotificacion {

    public void enviar(Contacto contacto, String mensaje) {
        EstrategiaDeNotificacion estrategia = crearEstrategia(contacto.getTipo());
        Notificacion notificacion = new Notificacion(contacto, mensaje);
        boolean exito = estrategia.enviar(notificacion);

        if (exito) {
            notificacion.marcarComoCompletada();
        } else {
            notificacion.marcarComoFallida();
        }
    }

    private EstrategiaDeNotificacion crearEstrategia(TipoContacto tipo) {
        return switch (tipo) {
            case EMAIL -> new NotificacionPorEmail();
            case SMS -> new NotificacionPorSMS();
            case WHATSAPP -> new NotificacionPorWhatsApp();
        };
    }
}