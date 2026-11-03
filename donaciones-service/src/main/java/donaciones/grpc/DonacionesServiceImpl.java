package donaciones.grpc;

import io.grpc.stub.StreamObserver;
import logistica.grpc.CambiarEstadoRequest;
import logistica.grpc.CambiarEstadoResponse;
import logistica.grpc.DonacionesServiceGrpc;

public class DonacionesServiceImpl extends DonacionesServiceGrpc.DonacionesServiceImplBase {

  @Override
  public void cambiarEstadoDonacion(CambiarEstadoRequest request, StreamObserver<CambiarEstadoResponse> responseObserver) {
    long idDonacion = request.getIdDonacion();
    String nuevoEstado = request.getNuevoEstado();

    // Acá aplicás la lógica de dominio o llamas al repository/controller correspondiente
    // Donacion donacion = donacionRepository.buscarPorId(idDonacion)...

    CambiarEstadoResponse response = CambiarEstadoResponse.newBuilder()
        .setExito(true)
        .setMensaje("Estado de donacion " + idDonacion + " actualizado a " + nuevoEstado)
        .build();

    responseObserver.onNext(response);
    responseObserver.onCompleted();
  }
}