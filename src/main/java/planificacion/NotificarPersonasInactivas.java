package planificacion;

import donante.Persona;
import notificacion.Notificacion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class NotificarPersonasInactivas {

  private static final Logger logger = LoggerFactory.getLogger(NotificarPersonasInactivas.class);
  private static final int DIAS_INACTIVIDAD = 20;

  public static List<Notificacion> notificarInactivos(List<Persona> personas) {
    List<Notificacion> historial = new ArrayList<>();
    LocalDateTime umbralInactividad = LocalDateTime.now().minusDays(DIAS_INACTIVIDAD);

    for (Persona persona : personas) {
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
      List<Persona> personasRegistradas = new ArrayList<>();
      notificarInactivos(personasRegistradas);
    } catch (RuntimeException e) {
      logger.error("Error al ejecutar tarea programada de notificación", e);
    }
    logger.info("Notificación de personas inactivas finalizada con éxito");
  }
}
