package planificacion;

import donaciones.controller.Notificador;
import donaciones.domain.donante.Persona;
import donaciones.domain.donante.RepositorioPersonas;
import donaciones.db.TransaccionPorRequest;
import donaciones.repository.NotificacionRepository;
import io.github.flbulgarelli.jpa.extras.simple.WithSimplePersistenceUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.List;

public class NotificarPersonasInactivas {

  private static final Logger logger = LoggerFactory.getLogger(NotificarPersonasInactivas.class);
  private static final int DIAS_INACTIVIDAD = 20;

  public static List<Persona> notificarInactivos(RepositorioPersonas repositorio, Notificador notificador) {
    List<Persona> inactivas = repositorio.buscarInactivasDesde(LocalDateTime.now().minusDays(DIAS_INACTIVIDAD));

    for (Persona persona : inactivas) {
      notificador.ausencia(persona, DIAS_INACTIVIDAD);
      logger.info("Se generó una notificación para la persona: {}", persona.getNombre());
    }
    return inactivas;
  }

  public static void main(String[] args) {
    logger.info("Iniciando notificación de personas inactivas");

    try {
      String dbUrl = System.getenv("DB_URL");
      if (dbUrl != null) {
        WithSimplePersistenceUnit.configure(properties -> properties.set("hibernate.connection.url", dbUrl));
      }
      RepositorioPersonas repositorio = new RepositorioPersonas();
      Notificador notificador = new Notificador(new NotificacionRepository());
      new TransaccionPorRequest().withTransaction(() -> {
        List<Persona> notificadas = notificarInactivos(repositorio, notificador);
        logger.info("Total de personas notificadas: {}", notificadas.size());
      });
      logger.info("Notificación de personas inactivas finalizada con éxito");
    } catch (RuntimeException e) {
      logger.error("Error al ejecutar tarea programada de notificación", e);
    }
  }
}
