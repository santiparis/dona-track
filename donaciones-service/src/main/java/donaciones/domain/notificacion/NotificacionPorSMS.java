package donaciones.domain.notificacion;

public class NotificacionPorSMS implements EstrategiaDeNotificacion {
    @Override
    public boolean enviar(String destino, String mensaje) {
        return true;
    }
}