package logistica.retrofit_client;

import retrofit2.Call;
import retrofit2.http.PATCH;
import retrofit2.http.Path;
import retrofit2.http.Query;

// Cliente hacia donaciones-service.
// Donaciones expone UN unico cambio de estado por donacion (lee id por path y estado/camion por query);
// segun el estado publica el evento correspondiente:
//   EN_TRASLADO -> InicioRuta, ENTREGADA -> EntregaRealizada, ENTREGA_FALLIDA -> EntregaNoSatisfactoria.
// Logistica informa el hecho; donaciones decide y aplica el cambio sobre su donacion.
// nombreCamion solo se usa para ENTREGADA (comprobante); si es null Retrofit omite el query param.
public interface DonacionesAPICalls {

  @PATCH("/api/donaciones/{id}/estado")
  Call<Void> cambiarEstado(@Path("id") String donacionId,
                           @Query("nuevo") String nuevoEstado,
                           @Query("nombreCamion") String nombreCamion);
}