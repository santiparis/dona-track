package logistica.mock;

import io.javalin.Javalin;
import logistica.domain.Camion;
import logistica.domain.Donacion;
import logistica.retrofit_client.PlanificacionCallbackRequest;
import logistica.retrofit_client.PlanificacionCallbackRequest.AsignacionCamion;
import logistica.retrofit_client.PlanificacionCallbackRequest.ParadaPlanificada;
import logistica.retrofit_client.PlanificadorAPICalls.PlanificacionRequest;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.POST;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Mock del componente EXTERNO de planificacion de rutas.
 *   1. recibe el POST con las donaciones asignadas + camiones disponibles,
 *   2. responde "OK" al instante (la integracion es asincronica),
 *   3. arma las rutas y, tras un delay, las devuelve por el callback de logistica.
 *
 */
public class MockPlanificador {

  // contrato del callback: donde el mock devuelve las rutas armadas a logistica
  interface LogisticaCallback {
    @POST("api/rutas")
    Call<Void> devolverRutas(@Body PlanificacionCallbackRequest resultado);
  }

  private static final LogisticaCallback CALLBACK = new Retrofit.Builder()
      .baseUrl("http://localhost:7070/")
      .addConverterFactory(JacksonConverterFactory.create())
      .build()
      .create(LogisticaCallback.class);

  public static void main(String[] args) {
    Javalin.create(config -> {
      config.routes.post("/planificador/donaciones", ctx -> {
        var request = ctx.bodyAsClass(PlanificacionRequest.class);
        // responde OK al toque, las rutas armadas van despues por el callback
        ctx.result("OK");
        // simula el procesamiento asincronico del proveedor
        CompletableFuture.runAsync(() -> devolverRutas(request));
      });
    }).start(9090);

    System.out.println("mock-planificador escuchando en :9090");
  }

  private static void devolverRutas(PlanificacionRequest request) {
    try {
      Thread.sleep(1500); // simula el tiempo de calculo del proveedor
      PlanificacionCallbackRequest resultado = planificar(request);
      Response<Void> resp = CALLBACK.devolverRutas(resultado).execute();
      System.out.println("mock-planificador: callback respondio " + resp.code());
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    } catch (Exception e) {
      System.err.println("mock-planificador: fallo al devolver rutas -> " + e.getMessage());
    }
  }

  /**
   * Planificacion simulada: una parada por entidad beneficiaria (agrupa por destino)
   * y le asigna TODAS las paradas al primer camion disponible. Si no hay camiones,
   * todo vuelve como "no asignado" para que logistica lo replanifique.
   */
  private static PlanificacionCallbackRequest planificar(PlanificacionRequest request) {
    List<Camion> camiones = request.camiones() == null ? List.of() : request.camiones();
    List<Donacion> donaciones = request.donaciones() == null ? List.of() : request.donaciones();

    if (camiones.isEmpty()) {
      return new PlanificacionCallbackRequest(List.of(), new ArrayList<>(donaciones));
    }

    // agrupo las donaciones por entidad -> cada grupo es una parada
    Map<String, List<Donacion>> porEntidad = new LinkedHashMap<>();
    for (Donacion d : donaciones) {
      porEntidad.computeIfAbsent(d.getEntidadNombre(), k -> new ArrayList<>()).add(d);
    }

    List<ParadaPlanificada> paradas = new ArrayList<>();
    for (List<Donacion> grupo : porEntidad.values()) {
      paradas.add(new ParadaPlanificada(grupo));
    }

    // todas las paradas van al primer camion
    AsignacionCamion asignacion = new AsignacionCamion(camiones.get(0).getPatente(), paradas);
    return new PlanificacionCallbackRequest(List.of(asignacion), List.of());
  }
}