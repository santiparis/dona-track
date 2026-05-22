package notificacion;

class NotificacionPorSMS implements EstrategiaDeNotificacion {
    @Override
    public boolean enviar(Notificacion notificacion) {
        String telefono = notificacion.getContacto().getValor();
        System.out.println("Simulando envío de SMS al teléfono: " + telefono);
        System.out.println("Mensaje: " + notificacion.getMensaje());
        return true;
    }
}