package logistica.repository;

import io.github.flbulgarelli.jpa.extras.test.SimplePersistenceTest;
import logistica.domain.Camion;
import logistica.domain.DonacionEncolada;
import logistica.domain.Entrega;
import logistica.domain.EstadoEntrega;
import logistica.domain.EstadoRuta;
import logistica.domain.Ruta;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class RutasRepositoryTest implements SimplePersistenceTest {

  private RutasRepository repositorio;
  private CamionesRepository camionesRepository;

  @BeforeEach
  void setUp() {
    repositorio = new RutasRepository();
    camionesRepository = new CamionesRepository();

    withTransaction(() -> {
      entityManager().createQuery("DELETE FROM DonacionEncolada").executeUpdate();
      entityManager().createQuery("DELETE FROM Entrega").executeUpdate();
      entityManager().createQuery("DELETE FROM Ruta").executeUpdate();
      entityManager().createQuery("DELETE FROM Camion").executeUpdate();
    });
  }

  // Helper adaptado con ID de donación variable
  private Ruta crearRutaConPatenteYDonacionId(String patente, Long donacionId) {
    Camion camion = new Camion(patente, 45.0, 3.2, 12000);
    camionesRepository.agregar(camion);

    Entrega entrega = new Entrega(
        List.of(new DonacionEncolada(donacionId, 20, "kg", "Av. Mitre 300", "Comedor Sol")),
        "Av. Mitre 300",
        "Comedor Sol");

    return new Ruta(camion, List.of(entrega));
  }

  @Test
  void guardarLaRutaGuardaSusEntregasYSusDonaciones() {
    Long[] rutaId = new Long[1];

    withTransaction(() -> {
      Ruta ruta = crearRutaConPatenteYDonacionId("AB101CD", 101L);
      repositorio.agregar(ruta);
      rutaId[0] = ruta.getId();
    });

    withTransaction(() -> {
      Ruta recuperada = repositorio.buscarPorId(rutaId[0]).orElseThrow();
      Entrega entrega = recuperada.getEntregas().get(0);

      assertEquals(1, recuperada.getEntregas().size());
      assertEquals(1, entrega.getListaDonaciones().size());
      assertEquals(101L, entrega.getListaDonaciones().get(0).getDonacionID());
    });
  }

  @Test
  void losEstadosVuelvenDeLaBase() {
    Long[] rutaId = new Long[1];

    withTransaction(() -> {
      Ruta ruta = crearRutaConPatenteYDonacionId("AB102CD", 102L);
      ruta.iniciar();
      repositorio.agregar(ruta);
      rutaId[0] = ruta.getId();
    });

    withTransaction(() -> {
      Ruta recuperada = repositorio.buscarPorId(rutaId[0]).orElseThrow();

      assertEquals(EstadoRuta.EN_CURSO, recuperada.getEstado());
      assertEquals(EstadoEntrega.EN_TRASLADO, recuperada.getEntregas().get(0).getEstado());
    });
  }

  @Test
  void laRutaRecuperadaConservaSuCamion() {
    Long[] rutaId = new Long[1];

    withTransaction(() -> {
      Ruta ruta = crearRutaConPatenteYDonacionId("AB103CD", 103L);
      repositorio.agregar(ruta);
      rutaId[0] = ruta.getId();
    });

    withTransaction(() -> {
      Ruta recuperada = repositorio.buscarPorId(rutaId[0]).orElseThrow();

      assertEquals("AB103CD", recuperada.getCamion().getPatente());
    });
  }

  @Test
  void seEncuentraLaRutaAPartirDeUnaDeSusEntregas() {
    Long[] ids = new Long[2];

    withTransaction(() -> {
      Ruta ruta = crearRutaConPatenteYDonacionId("AB104CD", 104L);
      repositorio.agregar(ruta);
      ids[0] = ruta.getId();
      ids[1] = ruta.getEntregas().get(0).getId();
    });

    withTransaction(() -> {
      Ruta encontrada = repositorio.buscarRutaPorEntregaId(ids[1]).orElseThrow();

      assertEquals(ids[0], encontrada.getId());
    });
  }

  @Test
  void buscarEntregaPorIdLaTraeSuelta() {
    Long[] entregaId = new Long[1];

    withTransaction(() -> {
      Ruta ruta = crearRutaConPatenteYDonacionId("AB105CD", 105L);
      repositorio.agregar(ruta);
      entregaId[0] = ruta.getEntregas().get(0).getId();
    });

    withTransaction(() -> {
      Entrega entrega = repositorio.buscarEntregaPorId(entregaId[0]).orElseThrow();

      assertEquals("Comedor Sol", entrega.getEntidadNombre());
    });
  }

  @Test
  void elCambioDeEstadoDeUnaEntregaSePersisteSinGuardarla() {
    Long[] entregaId = new Long[1];

    withTransaction(() -> {
      Ruta ruta = crearRutaConPatenteYDonacionId("AB106CD", 106L);
      repositorio.agregar(ruta);
      ruta.iniciar();
      entregaId[0] = ruta.getEntregas().get(0).getId();
    });

    withTransaction(() -> {
      repositorio.buscarEntregaPorId(entregaId[0]).orElseThrow().marcarEntregada();
    });

    withTransaction(() -> {
      Entrega entrega = repositorio.buscarEntregaPorId(entregaId[0]).orElseThrow();
      assertEquals(EstadoEntrega.ENTREGADA, entrega.getEstado());
      assertEquals("ENT-" + entregaId[0], entrega.getComprobante());
    });
  }

  @Test
  void buscarPorIdInexistenteDevuelveVacio() {
    withTransaction(() -> {
      assertTrue(repositorio.buscarPorId(999L).isEmpty());
    });
  }
}