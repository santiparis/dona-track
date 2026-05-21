package notificacion;

import donante.Contacto;
import donante.TipoContacto;

public class ServicioDeNotificacion {

    public void enviar(Contacto contacto, String mensaje) {
        EstrategiaDeNotificacion estrategia = crearEstrategia(contacto.getTipo());
        if (estrategia == null) {
            System.err.println("ADVERTENCIA: No se encontró una estrategia de notificación para el tipo: " + contacto.getTipo());
            return;
        }

        Notificacion notificacion = new Notificacion(contacto, mensaje);
        Notificador notificador = new Notificador(estrategia);
        notificador.enviar(notificacion);
    }

    private EstrategiaDeNotificacion crearEstrategia(TipoContacto tipo) {
        return switch (tipo) {
            case EMAIL -> new NotificacionPorEmail();
            case SMS -> new NotificacionPorSMS();
            case WHATSAPP -> new NotificacionPorWhatsApp();
        };
    }
}