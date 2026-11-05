package logistica.grpc;

import io.grpc.stub.StreamObserver;
import java.util.List;
import logistica.domain.Entrega;
import logistica.domain.Ruta;
import logistica.repository.RutasRepository;

import java.util.Optional;

public class LogisticaServiceImpl extends LogisticaServiceGrpc.LogisticaServiceImplBase {
  private final RutasRepository rutasRepository;

  public LogisticaServiceImpl(RutasRepository rutasRepo) {
    this.rutasRepository = rutasRepo;
  }

  // Los métodos son void porque devolvemos interactuando con el StreamObserver envíandole
  // eventos con sus métodos.
  @Override
  public void obtenerEstadoRuta(RutaRequest request, StreamObserver<RutaResponse> responseObserver) {
    String idRutaStr = request.getIdRuta();
    RutaResponse response;

    try {
      Long entregaId = Long.parseLong(idRutaStr);

      // Buscar la Entrega o Ruta en el RutasRepository
      Optional<Entrega> entregaOpt = rutasRepository.buscarEntregaPorId(entregaId);

      if (entregaOpt.isPresent()) {
        Entrega entrega = entregaOpt.get();

        // Buscar la Ruta asociada para obtener el ID del camión
        Optional<Ruta> rutaOpt = rutasRepository.buscarRutaPorEntregaId(entregaId);
        String idCamion = rutaOpt.flatMap(r -> Optional.ofNullable(r.getCamion()))
            .map(c -> String.valueOf(c.getId()))
            .orElse("SIN_ASIGNAR");

        response = RutaResponse.newBuilder()
            .setIdRuta(String.valueOf(entrega.getId()))
            .setEstado(entrega.getEstado() != null ? entrega.getEstado().name() : "DESCONOCIDO")
            .setIdCamion(idCamion)
            .setFechaEntrega(entrega.getComprobante() != null ? entrega.getComprobante() : "")
            .build();
      } else {
        response = RutaResponse.newBuilder()
            .setIdRuta(idRutaStr)
            .setEstado("NO_ENCONTRADO")
            .setIdCamion("")
            .setFechaEntrega("")
            .build();
      }
    } catch (NumberFormatException e) {
      response = RutaResponse.newBuilder()
          .setIdRuta(idRutaStr)
          .setEstado("ID_INVALIDO")
          .setIdCamion("")
          .setFechaEntrega("")
          .build();
    }

    responseObserver.onNext(response);
    responseObserver.onCompleted();
  }

  @Override
  public void trazarRuta(TrazarRutaRequest request, StreamObserver<TrazarRutaResponse> responseObserver) {
    try {
      TrazarRutaResponse response = TrazarRutaResponse.newBuilder()
          .setIdRuta("RUTA-" + request.getIdDonacion())
          .setExito(true)
          .setMensaje("Trazado registrado correctamente para la donación " + request.getIdDonacion())
          .build();

      responseObserver.onNext(response);
      responseObserver.onCompleted();
    } catch (Exception e) {
      TrazarRutaResponse response = TrazarRutaResponse.newBuilder()
          .setIdRuta("")
          .setExito(false)
          .setMensaje("Error al trazar ruta: " + e.getMessage())
          .build();

      responseObserver.onNext(response);
      responseObserver.onCompleted();
    }
  }

  // La request contiene una lista de ids de Donación.
  @Override
  public void obtenerUbicacionCamiones(UbicacionCamionesRequest request,
                                       StreamObserver<UbicacionCamionesResponse> responseObserver) {
    List<Long> idsDonaciones = request.getIdsDonacionesList();
    UbicacionCamionesResponse response;
    try {
      List<UbicacionCamion> ubicaciones =
          rutasRepository.obtenerUbicacionCamionesPorIdsDonaciones(idsDonaciones);

      response = UbicacionCamionesResponse.newBuilder()
          .addAllUbicaciones(ubicaciones)
          .build();
      responseObserver.onNext(response);
      responseObserver.onCompleted();
    } catch (Exception e) {
      responseObserver.onError(
          io.grpc.Status.INTERNAL
              .withDescription("Error al obtener las ubicaciones de los camiones: " + e.getMessage())
              .asRuntimeException()
      );
    }
  }
}