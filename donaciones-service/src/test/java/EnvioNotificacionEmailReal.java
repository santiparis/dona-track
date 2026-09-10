import donaciones.domain.donante.Genero;
import donaciones.domain.donante.PersonaHumana;
import donaciones.domain.donante.TipoDoc;
import donaciones.domain.notificacion.ContactoPorEmail;
import donaciones.domain.notificacion.EstadoNotificacion;
import donaciones.domain.notificacion.Notificacion;
import java.util.List;

/**
 * Clase principal (Main) independiente ubicada en el apartado de tests
 * para verificar y ejecutar envíos reales de notificaciones por Email (SendGrid).
 * -
 * Para ejecutar este envío real desde la terminal:
 * 1. Carga las variables de entorno de SendGrid:
 *    source sendgrid.env
 * 2. Ejecuta esta clase desde tu IDE o con Maven:
 *    mvn test-compile exec:java -pl donaciones-service -Dexec.mainClass="EnvioNotificacionEmailReal" -Dexec.classpathScope="test"
 * -
 * Para ejecutarlo desde el intelliJ:
 * 1. Editar la Run Configuration
 * 2. Agregar el path al sendgrid.env
 */
public class EnvioNotificacionEmailReal {

    public static void main(String[] args) {
        String emailDestino = (args.length > 0 && args[0] != null) ? args[0] : "juanignaciopereyra01@gmail.com";

        try {
            ContactoPorEmail contactoEmail = new ContactoPorEmail(emailDestino);
            PersonaHumana destinatario = new PersonaHumana(
                "Juan", "Pereyra", 25, TipoDoc.DNI, "12345678", Genero.MASCULINO,
                "Medrano 951", List.of(contactoEmail), contactoEmail, null
            );

            Notificacion notifEmail = destinatario.notificar("¡Hola! Esta es una prueba REAL por Email desde DonaTrack.");
            if (notifEmail != null && notifEmail.getEstado() == EstadoNotificacion.COMPLETADA) {
                System.out.println(" -> Envío Email finalizado con éxito.");
            }
        } catch (Exception ex) {
            System.err.println(" -> Error o falta de credenciales al intentar enviar Email: " + ex.getMessage());
        }
    }
}
