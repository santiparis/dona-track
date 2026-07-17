package logistica.service;

import io.javalin.Javalin;
import logistica.retrofit_client.PlanificadorAPICalls.PlanificacionRequest;
import logistica.retrofit_client.RetrofitConfig;
import logistica.domain.Camion;
import logistica.domain.Donacion;
import logistica.repository.CamionesRepository;
import logistica.repository.DonacionesRepository;
import logistica.repository.RutasRepository;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PlanificadorServiceTest {

  private static final int PORT = 8299;
  private static final AtomicInteger donacionesRecibidasPorElMock = new AtomicInteger();

  private static Javalin planificadorMock;

  private CamionesRepository camionesRepository;
  private RutasRepository rutasRepository;
  private DonacionesRepository donacionesRepository;
  private PlanificadorService planificadorService;

  @BeforeAll
  public static void levantarMockDelPlanificador() {
    planificadorMock = Javalin.create(config -> {
      config.routes.post("/planificador/donaciones", ctx -> {
        var body = ctx.bodyAsClass(PlanificacionRequest.class);
        donacionesRecibidasPorElMock.set(body.donaciones().size());
        ctx.json("\"OK\"");
      });
    }).start(PORT);
  }

  @AfterAll
  public static void apagarMockDelPlanificador() {
    if (planificadorMock != null) {
      planificadorMock.stop();
    }
  }

  @BeforeEach
  public void setUp() {
    donacionesRecibidasPorElMock.set(0);

    camionesRepository = new CamionesRepository();
    rutasRepository = new RutasRepository();
    donacionesRepository = new DonacionesRepository();
    camionesRepository.agregar(new Camion("AB123CD", 10, 2, 1000));

    var retrofitConfig = new RetrofitConfig();
    retrofitConfig.setPlanificadorBaseUrl("http://localhost:" + PORT + "/");

    planificadorService = new PlanificadorService(
        camionesRepository, rutasRepository, donacionesRepository, retrofitConfig.planificadorAPICalls());
  }

  @Test
  public void enviaLasDonacionesPendientesYLasSacaDelPoolSiElPlanificadorConfirma() throws IOException {
    donacionesRepository.agregar(new Donacion(1L, 1, "kg", "Av. Siempre Viva 742", "Comedor Sol"));
    donacionesRepository.agregar(new Donacion(2L, 2, "kg", "Av. Siempre Viva 742", "Comedor Sol"));

    planificadorService.enviarPlanificacion();

    assertEquals(2, donacionesRecibidasPorElMock.get());
    assertTrue(donacionesRepository.obtenerTodas().isEmpty());
  }

  @Test
  public void noHaceNadaSiNoHayDonacionesPendientes() throws IOException {
    planificadorService.enviarPlanificacion();

    assertEquals(0, donacionesRecibidasPorElMock.get());
  }

  @Test
  public void respetaElLimiteDeCienDonacionesPorBatch() throws IOException {
    for (int i = 0; i < 150; i++) {
      donacionesRepository.agregar(new Donacion((long) i, 1, "kg", "Av. Siempre Viva 742", "Comedor Sol"));
    }

    planificadorService.enviarPlanificacion();

    assertEquals(100, donacionesRecibidasPorElMock.get());
    assertEquals(50, donacionesRepository.obtenerTodas().size());
  }
}