package logistica.retrofit_client;
import logistica.domain.Camion;
import logistica.domain.Donacion;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

import java.util.List;

public interface PlanificadorAPICalls {

  public record PlanificacionRequest(List<Donacion> donaciones, List<Camion> camiones) {};
  // aca arranca la comunicacion con el planificador.
  // Void: la integracion es asincronica, solo importa el "OK" (status); las rutas llegan por callback
  @POST("/planificador/donaciones")
  Call<Void> enviarDonacionesAsignadas(@Body PlanificacionRequest request);

}
