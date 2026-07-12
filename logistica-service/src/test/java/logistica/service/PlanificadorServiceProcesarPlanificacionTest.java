package logistica.service;

import logistica.client.PlanificacionCallbackRequest;
import logistica.client.PlanificacionCallbackRequest.AsignacionCamion;
import logistica.client.PlanificacionCallbackRequest.ParadaPlanificada;
import logistica.domain.Camion;
import logistica.domain.Donacion;
import logistica.domain.Ruta;
import logistica.repository.CamionesRepository;
import logistica.repository.DonacionesRepository;
import logistica.repository.RutasRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class PlanificadorServiceProcesarPlanificacionTest {

  private RutasRepository rutasRepository;
  private DonacionesRepository donacionesRepository;
  private PlanificadorService planificadorService;

  @BeforeEach
  void setUp() {
    CamionesRepository camionesRepository = new CamionesRepository();
    camionesRepository.agregar(new Camion("AB123CD", 10, 2, 1000));

    rutasRepository = new RutasRepository();
    donacionesRepository = new DonacionesRepository();

    // procesarPlanificacion no usa el cliente Retrofit, por eso null acá
    planificadorService = new PlanificadorService(camionesRepository, rutasRepository, donacionesRepository, null);
  }

  @Test
  void creaUnaRutaConSuEntregaParaElCamionAsignado() {
    var donacion = new Donacion("D1", 1, "kg", null);
    var parada = new ParadaPlanificada(-34.6, -58.4, List.of(donacion));
    var asignacion = new AsignacionCamion("AB123CD", List.of(parada));
    var resultado = new PlanificacionCallbackRequest(List.of(asignacion), List.of());

    List<Ruta> rutasCreadas = planificadorService.procesarPlanificacion(resultado);

    assertEquals(1, rutasCreadas.size());
    assertEquals("AB123CD", rutasCreadas.get(0).getCamion().getPatente());
    assertEquals(1, rutasCreadas.get(0).getEntregas().size());
  }

  @Test
  void guardaLaRutaCreadaEnElRepositorio() {
    var parada = new ParadaPlanificada(-34.6, -58.4, List.of());
    var asignacion = new AsignacionCamion("AB123CD", List.of(parada));
    var resultado = new PlanificacionCallbackRequest(List.of(asignacion), List.of());

    planificadorService.procesarPlanificacion(resultado);

    assertEquals(1, rutasRepository.obtenerTodas().size());
  }

  @Test
  void reencolaLasDonacionesNoAsignadas() {
    var noAsignada = new Donacion("D2", 1, "kg", null);
    var resultado = new PlanificacionCallbackRequest(List.of(), List.of(noAsignada));

    planificadorService.procesarPlanificacion(resultado);

    assertEquals(1, donacionesRepository.obtenerTodas().size());
  }

  @Test
  void tiraExcepcionSiElCamionAsignadoNoExiste() {
    var parada = new ParadaPlanificada(-34.6, -58.4, List.of());
    var asignacion = new AsignacionCamion("PATENTE-INEXISTENTE", List.of(parada));
    var resultado = new PlanificacionCallbackRequest(List.of(asignacion), List.of());

    assertThrows(NoSuchElementException.class, () -> planificadorService.procesarPlanificacion(resultado));
  }
}