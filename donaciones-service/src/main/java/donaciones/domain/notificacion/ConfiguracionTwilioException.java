package donaciones.domain.notificacion;

public class ConfiguracionTwilioException extends RuntimeException {

    public ConfiguracionTwilioException(String mensaje) {
        super("Error en la configuración de Twilio: " + mensaje);
    }
}
