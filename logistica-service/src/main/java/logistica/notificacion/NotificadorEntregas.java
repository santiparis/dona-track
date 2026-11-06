package logistica.notificacion;

import java.util.List;
import logistica.domain.DonacionEncolada;
import logistica.domain.Entrega;
import logistica.grpc.DonacionesGrpcClient;
import logistica.grpc.CambiarEstadoResponse;

// Único punto del servicio que le habla a donaciones-service.
// Traduce los hechos de logística a los estados que entiende el servicio de donaciones mediante gRPC.
public class NotificadorEntregas {

  private static final String EN_TRASLADO = "EN_TRASLADO";
  private static final String ENTREGADA = "ENTREGADA";
  private static final String ENTREGA_FALLIDA = "ENTREGA_FALLIDA";

  private final DonacionesGrpcClient donacionesGrpcClient;

  public NotificadorEntregas(DonacionesGrpcClient donacionesGrpcClient) {
    this.donacionesGrpcClient = donacionesGrpcClient;
  }

  // TODO: Modificar el string de estos 3 métodos según corresponda
  public void avisarEnTraslado(List<Entrega> entregas) {
    entregas.forEach(entrega -> this.notificarEstado(entrega, EN_TRASLADO, "https://miratuenvio.com/ABC123"));
  }

  public void avisarEntregada(Entrega entrega, String datosAdicionales) {
    this.notificarEstado(entrega, ENTREGADA, datosAdicionales);
  }

  public void avisarFallida(Entrega entrega) {
    this.notificarEstado(entrega, ENTREGA_FALLIDA, "porque si");
  }

  // donaciones cambia el estado por donación; una entrega puede agrupar varias
  private void notificarEstado(Entrega entrega, String nuevoEstado, String datosAdicionales) {
    try {
      for (DonacionEncolada donacion : entrega.getListaDonaciones()) {
        CambiarEstadoResponse response = donacionesGrpcClient.cambiarEstadoDonacion(
            donacion.getDonacionID(),
            nuevoEstado,
            datosAdicionales
        );

        if (!response.getExito()) {
          System.err.printf("Error al notificar donación ID %d a donaciones-service: %s%n",
              donacion.getDonacionID(), response.getMensaje());
        }
      }
    } catch (Exception e) {
      throw new IllegalStateException("No se pudo notificar a donaciones-service vía gRPC", e);
    }
  }
}