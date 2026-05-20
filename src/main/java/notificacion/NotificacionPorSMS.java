package notificacion;

public class NotificacionPorSMS implements EstrategiaDeNotificacion {
    @Override
    public boolean enviar(Notificacion notificacion) {
        System.out.println("Simulando envío de SMS al teléfono: " + notificacion.getDestinatario());
        System.out.println("Mensaje: " + notificacion.getMensaje());
        // En una implementación real, aquí se llamaría al servicio de SMS
        // y se devolvería true o false según la respuesta.
        return true;
    }
}