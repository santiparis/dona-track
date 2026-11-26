package donaciones.grpc;

import donaciones.dto.DonacionLogisticaDTO;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import java.util.List;
import logistica.grpc.LogisticaServiceGrpc;
import logistica.grpc.ReportarEntregaRequest;
import logistica.grpc.ReportarEntregaResponse;
import logistica.grpc.RutaRequest;
import logistica.grpc.RutaResponse;
import logistica.grpc.TrazarRutaRequest;
import logistica.grpc.TrazarRutaResponse;
import logistica.grpc.UbicacionCamionesRequest;
import logistica.grpc.UbicacionCamionesResponse;

public class LogisticaGrpcClient {

  private final LogisticaServiceGrpc.LogisticaServiceBlockingStub blockingStub;

  public LogisticaGrpcClient(String host, int port) {
    // Canal de red hacia logistica-service (ej. localhost:9090)
    ManagedChannel channel = ManagedChannelBuilder.forAddress(host, port)
        .usePlaintext()
        .build();

    this.blockingStub = LogisticaServiceGrpc.newBlockingStub(channel);
  }

  public TrazarRutaResponse trazarRuta(DonacionLogisticaDTO donacion, String origen) {
    TrazarRutaRequest request = TrazarRutaRequest.newBuilder()
        .setIdDonacion(donacion.donacionID().toString())
        .setIdEntidadOrigen(origen != null ? origen : "")
        .setIdEntidadDestino(donacion.destino() != null ? donacion.destino() : "")
        .setCantidadBienes(donacion.cantidadBienes())
        .setUnidad(donacion.unidad() != null ? donacion.unidad() : "")
        .setEntidadNombre(donacion.entidadNombre() != null ? donacion.entidadNombre() : "")
        .build();

    return blockingStub.withDeadlineAfter(10, java.util.concurrent.TimeUnit.SECONDS).trazarRuta(request);
  }

  public RutaResponse consultarEstadoRuta(String idRuta) {
    RutaRequest request = RutaRequest.newBuilder()
        .setIdRuta(idRuta)
        .build();

    return blockingStub.obtenerEstadoRuta(request);
  }

  public UbicacionCamionesResponse obtenerUbicacionCamiones(List<Long> idsDonaciones) {
    if (idsDonaciones == null || idsDonaciones.isEmpty()) {
      return UbicacionCamionesResponse.newBuilder().build();
    }

    UbicacionCamionesRequest request = UbicacionCamionesRequest.newBuilder()
        .addAllIdsDonaciones(idsDonaciones)
        .build();

    return blockingStub.obtenerUbicacionCamiones(request);
  }

  public ReportarEntregaResponse reportarEntrega(Long entregaId, boolean exito, String justificacion) {
    ReportarEntregaRequest request = ReportarEntregaRequest.newBuilder()
        .setEntregaId(entregaId)
        .setExito(exito)
        .setJustificacion(justificacion != null ? justificacion : "")
        .build();
    return blockingStub.withDeadlineAfter(10, java.util.concurrent.TimeUnit.SECONDS).reportarEntrega(request);
  }
}
