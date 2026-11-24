package notificaciones;

import donaciones.domain.donante.Genero;
import donaciones.domain.donante.PersonaHumana;
import donaciones.domain.donante.TipoDoc;
import donaciones.domain.notificacion.ContactoPorSMS;
import donaciones.domain.notificacion.EstadoNotificacion;
import donaciones.domain.notificacion.Notificacion;

import java.util.List;

/**
 * Para ejecutar este envío real desde la terminal:
 * 1. Carga las variables de entorno de Twilio:
 *    source twilio.env
 * 2. Ejecuta esta clase desde tu IDE o con Maven, parado en la raíz del repo:
 *    mvn test-compile exec:java -pl donaciones-service -Dexec.mainClass="notificaciones.EnvioNotificacionSmsReal" -Dexec.classpathScope="test"
 * -
 * Para ejecutarlo desde el intelliJ:
 * 1. Editar la Run Configuration
 * 2. Agregar el path al twilio.env
 * -
 * Guía completa: documentacion/entrega4/notificaciones.md
 */
public class EnvioNotificacionSmsReal {

    public static void main(String[] args) {
        String telefonoDestino = (args.length > 0 && args[0] != null) ? args[0] : "+541144926571";

        try {
            ContactoPorSMS contactoSms = new ContactoPorSMS(telefonoDestino);
            PersonaHumana destinatario = new PersonaHumana(
                "Juan", "Pereyra", 25, TipoDoc.DNI, "12345678", Genero.MASCULINO,
                "Medrano 951", List.of(contactoSms), contactoSms, null
            );

            Notificacion notifSms = destinatario.notificar("¡Hola! Esta es una prueba REAL por SMS desde DonaTrack.");
            if (notifSms != null && notifSms.getEstado() == EstadoNotificacion.COMPLETADA) {
                System.out.println(" -> Envío SMS finalizado");
            } else {
                System.err.println(" -> El envío de SMS falló: revisar el error de Twilio en el log.");
            }
        } catch (Exception ex) {
            System.err.println(" -> Error al intentar enviar SMS: " + ex.getMessage());
        }
    }
}
