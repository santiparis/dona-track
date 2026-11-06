package donaciones.grpc;

import donaciones.domain.Donacion;
import donaciones.domain.EstadoDonacion;
import donaciones.repository.DonacionRepository;
import io.grpc.stub.StreamObserver;
import logistica.grpc.CambiarEstadoRequest;
import logistica.grpc.CambiarEstadoResponse;
import logistica.grpc.DonacionesServiceGrpc;

public class DonacionesServiceImpl extends DonacionesServiceGrpc.DonacionesServiceImplBase {

  private final DonacionRepository donacionRepository;

  public DonacionesServiceImpl(DonacionRepository donacionRepository) {
    this.donacionRepository = donacionRepository;
  }

  @Override
  public void cambiarEstadoDonacion(CambiarEstadoRequest request, StreamObserver<CambiarEstadoResponse> responseObserver) {
    long idDonacion = request.getIdDonacion();
    String nuevoEstadoStr = request.getNuevoEstado();

    try {
      donacionRepository.withTransaction(() -> {
        Donacion donacion = donacionRepository.buscarPorIdParaActualizar(idDonacion)
            .orElseThrow(() -> new IllegalArgumentException("No se encontró la donación con ID: " + idDonacion));
        donacion.cambiarEstado(EstadoDonacion.valueOf(nuevoEstadoStr), "Petición de cambio de Estado");
      });

      CambiarEstadoResponse response = CambiarEstadoResponse.newBuilder()
          .setExito(true)
          .setMensaje("Estado de donacion " + idDonacion + " actualizado a " + nuevoEstadoStr)
          .build();

      responseObserver.onNext(response);
      responseObserver.onCompleted();

    } catch (IllegalArgumentException e) {
      // Manejo de donación no encontrada o estado inválido
      CambiarEstadoResponse response = CambiarEstadoResponse.newBuilder()
          .setExito(false)
          .setMensaje(e.getMessage())
          .build();

      responseObserver.onNext(response);
      responseObserver.onCompleted();

    } catch (Exception e) {
      // Error inesperado
      CambiarEstadoResponse response = CambiarEstadoResponse.newBuilder()
          .setExito(false)
          .setMensaje("Error interno al actualizar estado: " + e.getMessage())
          .build();

      responseObserver.onNext(response);
      responseObserver.onCompleted();
    } finally {
      if (donacionRepository.perThreadEntityManagerAccess().isAttached()) {
        donacionRepository.perThreadEntityManagerAccess().dispose();
      }
    }
  }
}
