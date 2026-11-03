package donaciones.grpc;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import logistica.grpc.LogisticaServiceGrpc;
import logistica.grpc.RutaRequest;
import logistica.grpc.RutaResponse;
import logistica.grpc.TrazarRutaRequest;
import logistica.grpc.TrazarRutaResponse;

public class LogisticaGrpcClient {

  private final LogisticaServiceGrpc.LogisticaServiceBlockingStub blockingStub;

  public LogisticaGrpcClient(String host, int port) {
    // Canal de red hacia logistica-service (ej. localhost:9090)
    ManagedChannel channel = ManagedChannelBuilder.forAddress(host, port)
        .usePlaintext() // En desarrollo sin SSL/TLS
        .build();

    this.blockingStub = LogisticaServiceGrpc.newBlockingStub(channel);
  }

  public TrazarRutaResponse trazarRuta(String idDonacion, String origen, String destino) {
    TrazarRutaRequest request = TrazarRutaRequest.newBuilder()
        .setIdDonacion(idDonacion)
        .setIdEntidadOrigen(origen != null ? origen : "")
        .setIdEntidadDestino(destino != null ? destino : "")
        .build();

    return blockingStub.trazarRuta(request);
  }

  public RutaResponse consultarEstadoRuta(String idRuta) {
    RutaRequest request = RutaRequest.newBuilder()
        .setIdRuta(idRuta)
        .build();

    return blockingStub.obtenerEstadoRuta(request);
  }
}