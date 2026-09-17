package logistica.notificacion;

import java.io.IOException;
import java.util.List;
import logistica.domain.Donacion;
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

  public void avisarEnTraslado(List<Entrega> entregas) {
    entregas.forEach(entrega -> this.notificarEstado(entrega, EN_TRASLADO, null));
  }

  public void avisarEntregada(Entrega entrega, String patenteCamion) {
    this.notificarEstado(entrega, ENTREGADA, patenteCamion);
  }

  public void avisarFallida(Entrega entrega) {
    this.notificarEstado(entrega, ENTREGA_FALLIDA, null);
  }

  // donaciones cambia el estado por donacion; una entrega puede agrupar varias
  private void notificarEstado(Entrega entrega, String nuevoEstado, String patenteCamion) {
    try {
      for (Donacion donacion : entrega.getListaDonaciones()) {
        donacionesApi.cambiarEstado(donacion.getDonacionID(), new CambioEstadoDTO(nuevoEstado, patenteCamion)).execute();
      }
    } catch (IOException e) {
      throw new IllegalStateException("No se pudo notificar a donaciones-service", e);
    }
  }
}
