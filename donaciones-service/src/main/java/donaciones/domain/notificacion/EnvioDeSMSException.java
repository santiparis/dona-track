package donaciones.domain.notificacion;

public class EnvioDeSMSException extends RuntimeException {

    public EnvioDeSMSException(String detalle) {
        super("Hubo un error al intentar enviar la notificación por SMS: " + detalle);
    }
}
