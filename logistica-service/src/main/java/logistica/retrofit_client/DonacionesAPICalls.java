package logistica.retrofit_client;

import logistica.dto.CambioEstadoDTO;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.PATCH;
import retrofit2.http.Path;

// Cliente hacia donaciones-service.
// Donaciones expone UN unico cambio de estado por donacion (lee id por path y estado/datosAdicionales por body);
// segun el estado publica el evento correspondiente:
//   EN_TRASLADO -> InicioRuta, ENTREGADA -> EntregaRealizada, ENTREGA_FALLIDA -> EntregaNoSatisfactoria.
// Logistica informa el hecho; donaciones decide y aplica el cambio sobre su donacion.
public interface DonacionesAPICalls {

  @PATCH("/api/donaciones/{id}")
  Call<Void> cambiarEstado(@Path("id") Long donacionId,
                           @Body CambioEstadoDTO cambio);
}