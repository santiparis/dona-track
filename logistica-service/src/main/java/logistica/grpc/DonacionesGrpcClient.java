package logistica.grpc;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

public class DonacionesGrpcClient {

  private final DonacionesServiceGrpc.DonacionesServiceBlockingStub blockingStub;

  public DonacionesGrpcClient(String host, int port) {
    ManagedChannel channel = ManagedChannelBuilder.forAddress(host, port)
        .usePlaintext()
        .build();
    this.blockingStub = DonacionesServiceGrpc.newBlockingStub(channel);
  }

  public CambiarEstadoResponse cambiarEstadoDonacion(long idDonacion, String nuevoEstado, String detalles) {
    CambiarEstadoRequest request = CambiarEstadoRequest.newBuilder()
        .setIdDonacion(idDonacion)
        .setNuevoEstado(nuevoEstado)
        .setDetallesAdicionales(detalles != null ? detalles : "")
        .build();

    return blockingStub.cambiarEstadoDonacion(request);
  }
}