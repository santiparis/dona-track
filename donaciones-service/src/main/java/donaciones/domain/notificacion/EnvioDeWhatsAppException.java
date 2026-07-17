package donaciones.domain.notificacion;

public class EnvioDeWhatsAppException extends RuntimeException {

    public EnvioDeWhatsAppException(String detalle) {
        super("Hubo un error al intentar enviar la notificación por Whatsapp: " + detalle);
    }
}
