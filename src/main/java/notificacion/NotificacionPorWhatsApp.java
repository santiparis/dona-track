package notificacion;

class NotificacionPorWhatsApp implements EstrategiaDeNotificacion {
    @Override
    public boolean enviar(Notificacion notificacion) {
        System.out.println("Simulando envío de WhatsApp al teléfono: " + notificacion.getContacto().getValor());
        System.out.println("Mensaje: " + notificacion.getMensaje());
        return true;
    }
}