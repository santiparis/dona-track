package donaciones.domain.notificacion;

public class ConfiguracionSendGridException extends RuntimeException {

    public ConfiguracionSendGridException(String mensaje) {
        super("Error en la configuración de SendGrid: " + mensaje);
    }
}
