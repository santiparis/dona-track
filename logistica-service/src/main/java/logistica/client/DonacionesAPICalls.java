package logistica.client;

import logistica.domain.Entrega;
import logistica.domain.Ruta;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;


// necesita ejecutarse con .execute() -> sinc o .enqueue(callback) -> async
public interface DonacionesAPICalls {
  @POST("/donaciones/rutasIniciadas")
  // <String> para recibir "OK"
  Call<String> rutaIniciada(@Body Ruta ruta);

  @POST("/donaciones/entregaEstado")
  Call<String> entregaCompletada(@Body Entrega entrega);
  Call<String> entregaFallida(@Body Entrega entrega);

}
