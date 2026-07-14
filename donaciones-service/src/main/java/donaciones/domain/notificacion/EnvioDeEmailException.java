package donaciones.domain.notificacion;

public class EnvioDeEmailException extends RuntimeException {

    public EnvioDeEmailException(String detalle) {
        super("Hubo un error al intentar enviar la notificación por correo electrónico: " + detalle);
    }
}
