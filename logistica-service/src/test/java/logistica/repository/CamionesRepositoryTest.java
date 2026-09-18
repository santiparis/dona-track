package logistica.repository;

import io.github.flbulgarelli.jpa.extras.test.SimplePersistenceTest;
import logistica.domain.Camion;
import logistica.domain.Coordenadas;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

// SimplePersistenceTest abre una transaccion antes de cada test y hace rollback al final
public class CamionesRepositoryTest implements SimplePersistenceTest {

  private CamionesRepository repositorio;

  @BeforeEach
  void setUp() {
    repositorio = new CamionesRepository();
  }

  // el clear() vacia la cache del EntityManager: lo que se lee despues viene de la base,
  // no del objeto que quedo en memoria. Sin esto el test pasaria sin haber guardado nada.
  private void recargar() {
    entityManager().flush();
    entityManager().clear();
  }

  @Test
  void laBaseLeAsignaElIdAlGuardar() {
    Camion camion = new Camion("AB123CD", 45.0, 3.2, 12000);
    assertNull(camion.getId());

    repositorio.agregar(camion);

    assertTrue(camion.getId() > 0);
  }

  @Test
  void seGuardaYSeRecuperaConTodosSusDatos() {
    repositorio.agregar(new Camion("AB123CD", 45.0, 3.2, 12000));
    recargar();

    Camion recuperado = repositorio.buscarPorPatente("AB123CD").orElseThrow();

    assertEquals(45.0, recuperado.getVolumen());
    assertEquals(3.2, recuperado.getAltura());
    assertEquals(12000, recuperado.getCargaMax());
    assertTrue(recuperado.estaDisponible());
  }

  // el caso que rompia el mapeo: un camion recien dado de alta todavia no reporto posicion
  @Test
  void seGuardaUnCamionSinLocalizacion() {
    repositorio.agregar(new Camion("AB123CD", 45.0, 3.2, 12000));
    recargar();

    assertNull(repositorio.buscarPorPatente("AB123CD").orElseThrow().getLocalizacion());
  }

  // Coordenadas es @Embeddable: no es una tabla, son dos columnas mas de camiones
  @Test
  void laLocalizacionVuelveDeLaBase() {
    Camion camion = new Camion("AB123CD", 45.0, 3.2, 12000);
    camion.actualizarLocalizacion(new Coordenadas(-34.6, -58.4), 60);
    repositorio.agregar(camion);
    recargar();

    Coordenadas localizacion = repositorio.buscarPorId(camion.getId()).orElseThrow().getLocalizacion();

    assertEquals(-34.6, localizacion.getLatitud());
    assertEquals(-58.4, localizacion.getLongitud());
  }

  @Test
  void obtenerDisponiblesDejaAfueraALosOcupados() {
    Camion disponible = new Camion("AB123CD", 45.0, 3.2, 12000);
    Camion ocupado = new Camion("XY987ZW", 30.0, 2.8, 8000);
    ocupado.ocupar();
    repositorio.agregarTodos(List.of(disponible, ocupado));
    recargar();

    List<Camion> disponibles = repositorio.obtenerDisponibles();

    assertEquals(1, disponibles.size());
    assertEquals("AB123CD", disponibles.get(0).getPatente());
  }

  @Test
  void eliminarPorIdLoSacaDeLaBase() {
    Camion camion = new Camion("AB123CD", 45.0, 3.2, 12000);
    repositorio.agregar(camion);

    repositorio.eliminarPorId(camion.getId());
    recargar();

    assertTrue(repositorio.obtenerTodos().isEmpty());
  }
}
