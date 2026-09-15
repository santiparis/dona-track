package logistica.planificacion;

import java.io.IOException;
import java.util.List;
import logistica.domain.Camion;
import logistica.domain.Donacion;
import logistica.retrofit_client.PlanificadorAPICalls;
import logistica.retrofit_client.PlanificadorAPICalls.PlanificacionRequest;

// Unico punto del servicio que le habla al planificador externo.
// La integracion es asincronica: aca solo importa si acepto el pedido;
// las rutas armadas vuelven despues por el callback.
public class ClientePlanificador {

  private final PlanificadorAPICalls planificadorApi;

  public ClientePlanificador(PlanificadorAPICalls planificadorApi) {
    this.planificadorApi = planificadorApi;
  }

  public boolean enviarAPlanificar(List<Donacion> donaciones, List<Camion> camiones) {
    try {
      var request = new PlanificacionRequest(donaciones, camiones);
      return planificadorApi.enviarDonacionesAsignadas(request).execute().isSuccessful();
    } catch (IOException e) {
      throw new IllegalStateException("No se pudo contactar al planificador", e);
    }
  }
}
