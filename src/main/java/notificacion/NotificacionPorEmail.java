package notificacion;

public class NotificacionPorEmail implements EstrategiaDeNotificacion {
    @Override
    public boolean enviar(Notificacion notificacion) {
        System.out.println("Simulando envío de correo electrónico a: " + notificacion.getDestinatario());
        System.out.println("Mensaje: " + notificacion.getMensaje());
        return true;
    }
}