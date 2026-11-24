package donaciones.grpc;

import donaciones.controller.Notificador;
import donaciones.domain.Donacion;
import donaciones.domain.EstadoDonacion;
import donaciones.repository.DonacionRepository;
import donaciones.repository.PersonasAdministradorasRepository;
import io.grpc.stub.StreamObserver;
import logistica.grpc.CambiarEstadoRequest;
import logistica.grpc.CambiarEstadoResponse;
import logistica.grpc.DonacionesServiceGrpc;

public class DonacionesServiceImpl extends DonacionesServiceGrpc.DonacionesServiceImplBase {

  private final DonacionRepository donacionRepository;
  private final Notificador notificador;
  private final PersonasAdministradorasRepository administradorasRepository;

  public DonacionesServiceImpl(DonacionRepository donacionRepository, Notificador notificador,
                               PersonasAdministradorasRepository administradorasRepository) {
    this.donacionRepository = donacionRepository;
    this.notificador = notificador;
    this.administradorasRepository = administradorasRepository;
  }

  @Override
  public void cambiarEstadoDonacion(CambiarEstadoRequest request, StreamObserver<CambiarEstadoResponse> responseObserver) {
    long idDonacion = request.getIdDonacion();
    String nuevoEstadoStr = request.getNuevoEstado();

    try {
      EstadoDonacion nuevoEstado = EstadoDonacion.valueOf(nuevoEstadoStr);
      Donacion donacion = donacionRepository.withTransaction(() -> {
        Donacion encontrada = donacionRepository.buscarPorIdParaActualizar(idDonacion)
            .orElseThrow(() -> new IllegalArgumentException("No se encontró la donación con ID: " + idDonacion));
        encontrada.cambiarEstado(nuevoEstado, request.getDetallesAdicionales());
        return encontrada;
      });
      // Se avisa recién con el cambio confirmado; los avisos se guardan en su propia transacción
      donacionRepository.withTransaction(() -> this.avisar(donacion, nuevoEstado, request.getDetallesAdicionales()));

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

  // Igual que DonacionController.cambiarEstado; datosAdicionales depende del estado (url del mapa, comprobante o motivo)
  private void avisar(Donacion donacion, EstadoDonacion estado, String datosAdicionales) {
    switch (estado) {
      case EN_TRASLADO -> notificador.inicioDeTraslado(donacion, datosAdicionales);
      case ENTREGADA -> notificador.entregaConfirmada(donacion, datosAdicionales);
      case ENTREGA_FALLIDA -> notificador.entregaFallida(donacion, datosAdicionales, administradorasRepository.obtenerTodos());
      default -> { }
    }
  }
}
