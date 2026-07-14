package logistica.retrofit_client;

import logistica.domain.Entrega;
import logistica.domain.Ruta;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;


// necesita ejecutarse con .execute() -> sinc o .enqueue(callback) -> async
// Void: solo interesa el status HTTP; Retrofit ignora el body de la respuesta
public interface DonacionesAPICalls {
  @POST("/donaciones/rutasIniciadas")
  Call<Void> rutaIniciada(@Body Ruta ruta);

  @POST("/donaciones/entregaCompletada")
  Call<Void> entregaCompletada(@Body Entrega entrega);

  @POST("/donaciones/entregaFallida")
  Call<Void> entregaFallida(@Body Entrega entrega);

}
