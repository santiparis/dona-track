package notificacion;

class NotificacionPorSMS implements EstrategiaDeNotificacion {
    @Override
    public boolean enviar(Notificacion notificacion) {
        System.out.println("Simulando envío de SMS al teléfono: " + notificacion.getContacto().getValor());
        System.out.println("Mensaje: " + notificacion.getMensaje());
        return true;
    }
}