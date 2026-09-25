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
  private Ruta ruta;

  @BeforeEach
  void setUp() {
    repositorio = new RutasRepository();
    camionesRepository = new CamionesRepository();

    Camion camion = new Camion("AB123CD", 45.0, 3.2, 12000);
    camionesRepository.agregar(camion);

    Entrega entrega = new Entrega(
        List.of(new DonacionEncolada(7L, 20, "kg", "Av. Mitre 300", "Comedor Sol")),
        "Av. Mitre 300",
        "Comedor Sol");

    ruta = new Ruta(camion, List.of(entrega));
  }

  private void recargar() {
    entityManager().flush();
    entityManager().clear();
  }

  @Test
  void guardarLaRutaGuardaSusEntregasYSusDonaciones() {
    repositorio.agregar(ruta);
    recargar();

    Ruta recuperada = repositorio.buscarPorId(ruta.getId()).orElseThrow();
    Entrega entrega = recuperada.getEntregas().get(0);

    assertEquals(1, recuperada.getEntregas().size());
    assertEquals(1, entrega.getListaDonaciones().size());
    assertEquals(7L, entrega.getListaDonaciones().get(0).getDonacionID());
  }

  @Test
  void losEstadosVuelvenDeLaBase() {
    ruta.iniciar();
    repositorio.agregar(ruta);
    recargar();

    Ruta recuperada = repositorio.buscarPorId(ruta.getId()).orElseThrow();

    assertEquals(EstadoRuta.EN_CURSO, recuperada.getEstado());
    assertEquals(EstadoEntrega.EN_TRASLADO, recuperada.getEntregas().get(0).getEstado());
  }

  @Test
  void laRutaRecuperadaConservaSuCamion() {
    repositorio.agregar(ruta);
    recargar();

    Ruta recuperada = repositorio.buscarPorId(ruta.getId()).orElseThrow();

    assertEquals("AB123CD", recuperada.getCamion().getPatente());
  }

  @Test
  void seEncuentraLaRutaAPartirDeUnaDeSusEntregas() {
    repositorio.agregar(ruta);
    Long entregaId = ruta.getEntregas().get(0).getId();
    recargar();

    Ruta encontrada = repositorio.buscarRutaPorEntregaId(entregaId).orElseThrow();

    assertEquals(ruta.getId(), encontrada.getId());
  }

  @Test
  void buscarEntregaPorIdLaTraeSuelta() {
    repositorio.agregar(ruta);
    Long entregaId = ruta.getEntregas().get(0).getId();
    recargar();

    Entrega entrega = repositorio.buscarEntregaPorId(entregaId).orElseThrow();

    assertEquals("Comedor Sol", entrega.getEntidadNombre());
  }

  @Test
  void elCambioDeEstadoDeUnaEntregaSePersisteSinGuardarla() {
    repositorio.agregar(ruta);
    ruta.iniciar();
    Long entregaId = ruta.getEntregas().get(0).getId();
    recargar();

    repositorio.buscarEntregaPorId(entregaId).orElseThrow().marcarEntregada();
    recargar();

    Entrega entrega = repositorio.buscarEntregaPorId(entregaId).orElseThrow();
    assertEquals(EstadoEntrega.ENTREGADA, entrega.getEstado());
    assertEquals("ENT-" + entregaId, entrega.getComprobante());
  }

  @Test
  void buscarPorIdInexistenteDevuelveVacio() {
    assertTrue(repositorio.buscarPorId(999L).isEmpty());
  }
}
