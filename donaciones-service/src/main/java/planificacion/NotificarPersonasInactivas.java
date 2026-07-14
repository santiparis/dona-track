package planificacion;

import donaciones.domain.donante.Persona;
import donaciones.domain.donante.RepositorioPersonas;
import donaciones.domain.notificacion.Notificacion;
import donaciones.domain.donante.Contacto;
import donaciones.domain.donante.Genero;
import donaciones.domain.donante.PersonaHumana;
import donaciones.domain.donante.TipoDoc;
import donaciones.domain.notificacion.EstrategiaDeNotificacion;
import donaciones.domain.notificacion.NotificacionPorEmail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class NotificarPersonasInactivas {

  private static final Logger logger = LoggerFactory.getLogger(NotificarPersonasInactivas.class);
  private static final int DIAS_INACTIVIDAD = 20;

  public static List<Notificacion> notificarInactivos(RepositorioPersonas repositorio) {
    List<Notificacion> historial = new ArrayList<>();
    LocalDateTime umbralInactividad = LocalDateTime.now().minusDays(DIAS_INACTIVIDAD);

    for (Persona persona : repositorio.obtenerTodas()) {
      if (persona.getUltimaInteraccion().isBefore(umbralInactividad)) {
        String mensaje = "¡Hola " + persona.getNombre() + "! Notamos más de " + DIAS_INACTIVIDAD
            + " días sin actividad. ¡Te invitamos a realizar una nueva donación en la plataforma!";
        Notificacion notificacion = persona.notificar(mensaje);
        if (notificacion != null) {
          historial.add(notificacion);
        }
        logger.info("Se generó una notificación para la persona: {}", persona.getNombre());
      }
    }
    return historial;
  }

  public static void main(String[] args) {
    logger.info("Iniciando notificación de personas inactivas");

    try {
      RepositorioPersonas repositorio = new RepositorioPersonas();
      poblarDatosDePrueba(repositorio);
      List<Notificacion> notificaciones = notificarInactivos(repositorio);
      logger.info("Total de personas notificadas en la prueba: {}", notificaciones.size());
      logger.info("Notificación de personas inactivas finalizada con éxito");
    } catch (RuntimeException e) {
      logger.error("Error al ejecutar tarea programada de notificación", e);
    }
  }

  private static void poblarDatosDePrueba(RepositorioPersonas repositorio) {
    EstrategiaDeNotificacion email = new NotificacionPorEmail();

    // 1. Usuario inactivo (25 días sin actividad > umbral de 20 días): SÍ debe recibir notificación
    Contacto contactoInactivo = new Contacto(email, "juanignaciopereyra01@gmail.com");
    PersonaHumana donanteInactivo = new PersonaHumana(
        "Carlos Inactivo", "Pérez", 45, TipoDoc.DNI, "11111111", Genero.MASCULINO,
        "Av. Siempre Viva 742", List.of(contactoInactivo), contactoInactivo, null
    );
    donanteInactivo.setUltimaInteraccion(LocalDateTime.now().minusDays(25));

    // 2. Usuario activo (5 días sin actividad <= umbral de 20 días): NO debe recibir notificación
    Contacto contactoActivo = new Contacto(email, "activo@donante.org");
    PersonaHumana donanteActivo = new PersonaHumana(
        "Ana Activa", "López", 30, TipoDoc.DNI, "22222222", Genero.FEMENINO,
        "Calle Falsa 123", List.of(contactoActivo), contactoActivo, null
    );
    donanteActivo.setUltimaInteraccion(LocalDateTime.now().minusDays(5));

    repositorio.agregar(donanteInactivo);
    repositorio.agregar(donanteActivo);
    logger.info("Repositorio poblado con 2 personas de prueba (1 inactiva con 25 días, 1 activa con 5 días)");
  }
}
