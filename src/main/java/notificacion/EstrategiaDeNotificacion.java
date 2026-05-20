package notificacion;

public interface EstrategiaDeNotificacion {
    boolean enviar(Notificacion notificacion);
}