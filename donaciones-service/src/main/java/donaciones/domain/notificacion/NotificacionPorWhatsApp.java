package donaciones.domain.notificacion;

public class NotificacionPorWhatsApp implements EstrategiaDeNotificacion {
    @Override
    public boolean enviar(String destino, String mensaje) {
        return true;
    }
}