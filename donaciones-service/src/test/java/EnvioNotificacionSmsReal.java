import donaciones.domain.donante.Contacto;
import donaciones.domain.notificacion.EstadoNotificacion;
import donaciones.domain.notificacion.Notificacion;
import donaciones.domain.notificacion.NotificacionPorSMS;

/**
 * Para ejecutar este envío real desde la terminal:
 * 1. Carga las variables de entorno de Twilio:
 *    source twilio.env
 * 2. Ejecuta esta clase desde tu IDE o con Maven:
 *    mvn test-compile exec:java -pl donaciones-service -Dexec.mainClass="EnvioNotificacionSmsReal" -Dexec.classpathScope="test"
 * -
 * Para ejecutarlo desde el intelliJ:
 * 1. Editar la Run Configuration
 * 2. Agregar el path al twilio.env
 */
public class EnvioNotificacionSmsReal {

    public static void main(String[] args) {
        String telefonoDestino = (args.length > 0 && args[0] != null) ? args[0] : "+541144926571";

        try {
            Contacto contactoSms = new Contacto(new NotificacionPorSMS(), telefonoDestino);
            Notificacion notifSms = new Notificacion(contactoSms, "¡Hola! Esta es una prueba REAL por SMS desde DonaTrack.");
            notifSms.enviar();
            if (notifSms.getEstado() == EstadoNotificacion.COMPLETADA) {
                System.out.println(" -> Envío SMS finalizado");
            }
        } catch (Exception ex) {
            System.err.println(" -> Error al intentar enviar SMS: " + ex.getMessage());
        };
    }
}
