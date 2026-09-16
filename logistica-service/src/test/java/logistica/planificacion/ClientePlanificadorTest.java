package logistica.planificacion;

import io.javalin.Javalin;
import logistica.domain.Camion;
import logistica.domain.Donacion;
import logistica.retrofit_client.PlanificadorAPICalls;
import logistica.retrofit_client.PlanificadorAPICalls.PlanificacionRequest;
import logistica.retrofit_client.RetrofitConfig;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

// Prueba el envio real por Retrofit contra un planificador mockeado con Javalin.
// Es el unico test que verifica que el request se serialice y viaje bien.
public class ClientePlanificadorTest {

  private static final int PORT = 8299;
  private static final AtomicInteger donacionesRecibidas = new AtomicInteger();
  private static final AtomicInteger camionesRecibidos = new AtomicInteger();
  private static final AtomicInteger statusARespoder = new AtomicInteger(200);

  private static Javalin planificadorMock;

  private ClientePlanificador clientePlanificador;

  @BeforeAll
  static void levantarMockDelPlanificador() {
    planificadorMock = Javalin.create(config ->
        config.routes.post("/planificador/donaciones", ctx -> {
          var body = ctx.bodyAsClass(PlanificacionRequest.class);
          donacionesRecibidas.set(body.donaciones().size());
          camionesRecibidos.set(body.camiones().size());
          ctx.status(statusARespoder.get()).json("\"OK\"");
        })
    ).start(PORT);
  }

  @AfterAll
  static void apagarMockDelPlanificador() {
    if (planificadorMock != null) {
      planificadorMock.stop();
    }
  }

  @BeforeEach
  void setUp() {
    donacionesRecibidas.set(0);
    camionesRecibidos.set(0);
    statusARespoder.set(200);
    clientePlanificador = new ClientePlanificador(configApuntandoA(PORT));
  }

  @Test
  void mandaLasDonacionesYLosCamionesAlPlanificador() {
    boolean acepto = clientePlanificador.enviarAPlanificar(
        List.of(unaDonacion(1L), unaDonacion(2L)),
        List.of(new Camion("AB123CD", 10, 2, 1000))
    );

    assertTrue(acepto);
    assertEquals(2, donacionesRecibidas.get());
    assertEquals(1, camionesRecibidos.get());
  }

  @Test
  void devuelveFalseSiElPlanificadorRechazaElPedido() {
    statusARespoder.set(500);

    boolean acepto = clientePlanificador.enviarAPlanificar(List.of(unaDonacion(1L)), List.of());

    assertFalse(acepto);
  }

  @Test
  void tiraExcepcionSiElPlanificadorNoResponde() {
    // puerto sin nadie escuchando
    var clienteSinServidor = new ClientePlanificador(configApuntandoA(8298));

    assertThrows(IllegalStateException.class,
        () -> clienteSinServidor.enviarAPlanificar(List.of(unaDonacion(1L)), List.of()));
  }

  private PlanificadorAPICalls configApuntandoA(int puerto) {
    var retrofitConfig = new RetrofitConfig();
    retrofitConfig.setPlanificadorBaseUrl("http://localhost:" + puerto + "/");
    return retrofitConfig.planificadorAPICalls();
  }

  private Donacion unaDonacion(Long id) {
    return new Donacion(id, 1, "kg", "Av. Siempre Viva 742", "Comedor Sol");
  }
}
