package notificacion;

interface EstrategiaDeNotificacion {
    boolean enviar(Notificacion notificacion);
}