package logistica.controller;

import io.github.flbulgarelli.jpa.extras.test.SimplePersistenceTest;
import io.javalin.http.Context;
import logistica.domain.Camion;
import logistica.domain.DonacionEncolada;
import logistica.domain.Ruta;
import logistica.planificacion.ClientePlanificador;
import logistica.repository.CamionesRepository;
import logistica.repository.DonacionesRepository;
import logistica.repository.RutasRepository;
import logistica.retrofit_client.PlanificacionCallbackRequest;
import logistica.retrofit_client.PlanificacionCallbackRequest.AsignacionCamion;
import logistica.retrofit_client.PlanificacionCallbackRequest.ParadaPlanificada;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

// usa un CamionesRepository real, asi que necesita una transaccion: la abre el mixin
public class PlanificadorControllerTest implements SimplePersistenceTest {

  private CamionesRepository camionesRepository;
  private RutasRepository rutasRepository;
  private DonacionesRepository donacionesRepository;
  private ClientePlanificador clientePlanificador;
  private PlanificadorController controller;
  private Context ctx;

  @BeforeEach
  void setUp() {
    camionesRepository = new CamionesRepository();
    camionesRepository.agregar(new Camion("AB123CD", 10, 2, 1000));
    rutasRepository = new RutasRepository();
    donacionesRepository = new DonacionesRepository();
    clientePlanificador = mock(ClientePlanificador.class);

    controller = new PlanificadorController(
        camionesRepository, rutasRepository, donacionesRepository, clientePlanificador);
    ctx = mock(Context.class, RETURNS_DEEP_STUBS);
  }

  // ---------- planificar ----------

  @Test
  void planificarEnviaLasPendientesYLasSacaDelPoolSiElPlanificadorConfirma() {
    donacionesRepository.agregar(unaDonacion(1L));
    donacionesRepository.agregar(unaDonacion(2L));
    when(clientePlanificador.enviarAPlanificar(anyList(), anyList())).thenReturn(true);

    controller.planificar(ctx);

    assertTrue(donacionesRepository.obtenerTodas().isEmpty());
    verify(ctx).status(202);
  }

  @Test
  void planificarNoSacaNadaDelPoolSiElPlanificadorNoConfirma() {
    donacionesRepository.agregar(unaDonacion(1L));
    when(clientePlanificador.enviarAPlanificar(anyList(), anyList())).thenReturn(false);

    controller.planificar(ctx);

    assertEquals(1, donacionesRepository.obtenerTodas().size());
  }

  @Test
  void planificarNoLlamaAlPlanificadorSiNoHayDonacionesPendientes() {
    controller.planificar(ctx);

    verify(clientePlanificador, never()).enviarAPlanificar(anyList(), anyList());
    verify(ctx).status(200);
  }

  @Test
  void planificarRespetaElLimiteDeCienDonacionesPorBatch() {
    for (int i = 0; i < 150; i++) {
      donacionesRepository.agregar(unaDonacion((long) i));
    }
    when(clientePlanificador.enviarAPlanificar(anyList(), anyList())).thenReturn(true);

    controller.planificar(ctx);

    // se mandan 100 y las 50 restantes quedan para la proxima corrida
    verify(clientePlanificador).enviarAPlanificar(
        argThat(lista -> lista.size() == 100), anyList());
    assertEquals(50, donacionesRepository.obtenerTodas().size());
  }

  @Test
  void planificarDevuelveBadGatewaySiNoSePuedeContactarAlPlanificador() {
    donacionesRepository.agregar(unaDonacion(1L));
    when(clientePlanificador.enviarAPlanificar(anyList(), anyList()))
        .thenThrow(new IllegalStateException("No se pudo contactar al planificador"));

    controller.planificar(ctx);

    verify(ctx).status(502);
    // si no se pudo mandar, las donaciones no se pierden
    assertEquals(1, donacionesRepository.obtenerTodas().size());
  }

  // ---------- callback con las rutas armadas ----------

  @Test
  void obtenerRutasCreaLaRutaConSuEntregaYLaGuarda() {
    when(ctx.bodyAsClass(PlanificacionCallbackRequest.class))
        .thenReturn(callbackCon("AB123CD", unaDonacion(1L)));

    controller.obtenerRutas(ctx);

    List<Ruta> guardadas = rutasRepository.obtenerTodas();
    assertEquals(1, guardadas.size());
    assertEquals("AB123CD", guardadas.get(0).getCamion().getPatente());
    assertEquals(1, guardadas.get(0).getEntregas().size());
    verify(ctx).status(201);
  }

  @Test
  void obtenerRutasReencolaLasDonacionesNoAsignadas() {
    var noAsignada = unaDonacion(2L);
    when(ctx.bodyAsClass(PlanificacionCallbackRequest.class))
        .thenReturn(new PlanificacionCallbackRequest(List.of(), List.of(noAsignada)));

    controller.obtenerRutas(ctx);

    assertEquals(1, donacionesRepository.obtenerTodas().size());
  }

  @Test
  void obtenerRutasDevuelveBadRequestCuandoElCallbackEsInvalido() {
    when(ctx.bodyAsClass(PlanificacionCallbackRequest.class))
        .thenThrow(new IllegalArgumentException("Body invalido"));

    controller.obtenerRutas(ctx);

    verify(ctx).status(400);
  }

  @Test
  void obtenerRutasDevuelveNotFoundCuandoElCamionNoExiste() {
    when(ctx.bodyAsClass(PlanificacionCallbackRequest.class))
        .thenReturn(callbackCon("PATENTE-INEXISTENTE", unaDonacion(1L)));

    controller.obtenerRutas(ctx);

    verify(ctx).status(404);
  }

  @Test
  void obtenerRutasNoGuardaNadaSiUnCamionDelPlanNoExiste() {
    var asignacionValida = new AsignacionCamion("AB123CD", List.of(new ParadaPlanificada(List.of(unaDonacion(1L)))));
    var asignacionRota = new AsignacionCamion("NO-EXISTE", List.of(new ParadaPlanificada(List.of(unaDonacion(2L)))));
    when(ctx.bodyAsClass(PlanificacionCallbackRequest.class))
        .thenReturn(new PlanificacionCallbackRequest(List.of(asignacionValida, asignacionRota), List.of()));

    controller.obtenerRutas(ctx);

    // el callback es un plan completo: o entra entero o no entra nada
    assertTrue(rutasRepository.obtenerTodas().isEmpty());
    verify(ctx).status(404);
  }

  private DonacionEncolada unaDonacion(Long id) {
    return new DonacionEncolada(id, 1, "kg", "Av. Siempre Viva 742", "Comedor Sol");
  }

  private PlanificacionCallbackRequest callbackCon(String patente, DonacionEncolada donacionEncolada) {
    var parada = new ParadaPlanificada(List.of(donacionEncolada));
    return new PlanificacionCallbackRequest(List.of(new AsignacionCamion(patente, List.of(parada))), List.of());
  }
}
