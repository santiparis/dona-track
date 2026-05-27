package notificacion;

public class NotificacionPorEmail implements EstrategiaDeNotificacion {
    @Override
    public boolean enviar(String destino, String mensaje) {
        return true;
    }
}