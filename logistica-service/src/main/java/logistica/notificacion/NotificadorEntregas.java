package logistica.notificacion;

import java.io.IOException;
import java.util.List;
import logistica.domain.DonacionEncolada;
import logistica.domain.Entrega;
import logistica.dto.CambioEstadoDTO;
import logistica.retrofit_client.DonacionesAPICalls;

// Unico punto del servicio que le habla a donaciones-service.
// Traduce los hechos de logistica a los estados que entiende la otra API.
public class NotificadorEntregas {

  private static final String EN_TRASLADO = "EN_TRASLADO";
  private static final String ENTREGADA = "ENTREGADA";
  private static final String ENTREGA_FALLIDA = "ENTREGA_FALLIDA";

  private final DonacionesAPICalls donacionesApi;

  public NotificadorEntregas(DonacionesAPICalls donacionesApi) {
    this.donacionesApi = donacionesApi;
  }

  //TODO: Modificar el string de estos 3 metodos
  public void avisarEnTraslado(List<Entrega> entregas) {
    entregas.forEach(entrega -> this.notificarEstado(entrega, EN_TRASLADO, "https://miratuenvio.com/ABC123"));
  }

  public void avisarEntregada(Entrega entrega, String datosAdicionales) {
    this.notificarEstado(entrega, ENTREGADA, datosAdicionales);
  }

  public void avisarFallida(Entrega entrega) {
    this.notificarEstado(entrega, ENTREGA_FALLIDA, "placeholder");
  }

  // donaciones cambia el estado por donacion; una entrega puede agrupar varias
  private void notificarEstado(Entrega entrega, String nuevoEstado, String datosAdicionales) {
    try {
      for (DonacionEncolada donacion : entrega.getListaDonaciones()) {
        donacionesApi.cambiarEstado(donacion.getDonacionID(), new CambioEstadoDTO(nuevoEstado, datosAdicionales)).execute();
      }
    } catch (IOException e) {
      throw new IllegalStateException("No se pudo notificar a donaciones-service", e);
    }
  }
}
