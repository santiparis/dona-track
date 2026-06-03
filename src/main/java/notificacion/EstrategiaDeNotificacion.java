package notificacion;

public interface EstrategiaDeNotificacion {
    boolean enviar(String destino, String mensaje);
}