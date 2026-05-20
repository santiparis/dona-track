package notificacion;

public class NotificacionPorWhatsApp implements EstrategiaDeNotificacion {
    @Override
    public boolean enviar(Notificacion notificacion) {
        System.out.println("Simulando envío de WhatsApp al teléfono: " + notificacion.getDestinatario());
        System.out.println("Mensaje: " + notificacion.getMensaje());
        // En una implementación real, aquí se llamaría al servicio de WhatsApp
        // y se devolvería true o false según la respuesta.
        return true;
    }
}