package logistica.grpc;

import io.grpc.stub.StreamObserver;
import java.util.List;
import logistica.domain.Camion;
import logistica.domain.DonacionEncolada;
import logistica.domain.Entrega;
import logistica.domain.Ruta;
import logistica.db.TransaccionPorRequest;
import logistica.notificacion.NotificadorEntregas;
import logistica.repository.CamionesRepository;
import logistica.repository.DonacionesRepository;
import logistica.repository.RutasRepository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.NoSuchElementException;
import java.util.Optional;

public class LogisticaServiceImpl extends LogisticaServiceGrpc.LogisticaServiceImplBase {
  private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

  private final RutasRepository rutasRepository;
  private final DonacionesRepository donacionesRepository;
  private final NotificadorEntregas notificadorEntregas;
  private final TransaccionPorRequest transaccion;
  private final CamionesRepository camionesRepository;

  public LogisticaServiceImpl(RutasRepository rutasRepo, DonacionesRepository donacionesRepository,
                             NotificadorEntregas notificadorEntregas, TransaccionPorRequest transaccion,
                             CamionesRepository camionesRepository) {
    this.rutasRepository = rutasRepo;
    this.donacionesRepository = donacionesRepository;
    this.notificadorEntregas = notificadorEntregas;
    this.transaccion = transaccion;
    this.camionesRepository = camionesRepository;
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
    TrazarRutaResponse response;
    try {
      long idDonacion = Long.parseLong(request.getIdDonacion());
      if (idDonacion <= 0 || request.getCantidadBienes() <= 0 || request.getIdEntidadDestino().isBlank()) {
        throw new IllegalArgumentException("Se requiere un ID, una cantidad positiva y un destino");
      }
      var donacion = new DonacionEncolada(
          idDonacion, request.getCantidadBienes(), request.getUnidad(),
          request.getIdEntidadDestino(), request.getEntidadNombre());
      transaccion.withTransaction(() -> donacionesRepository.agregarSiNoExiste(donacion));

      response = TrazarRutaResponse.newBuilder()
          .setExito(true)
          .setMensaje("Donación " + idDonacion + " registrada para planificación")
          .build();
    } catch (Exception e) {
      response = TrazarRutaResponse.newBuilder()
          .setExito(false)
          .setMensaje("No se pudo encolar la donación: " + e.getMessage())
          .build();
    } finally {
      if (transaccion.perThreadEntityManagerAccess().isAttached()) {
        transaccion.perThreadEntityManagerAccess().dispose();
      }
    }
    responseObserver.onNext(response);
    responseObserver.onCompleted();
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

  @Override
  public void reportarEntrega(ReportarEntregaRequest request, StreamObserver<ReportarEntregaResponse> responseObserver) {
    try {
      Long donacionId = request.getEntregaId();

      // Lo mismo que EntregasController.confirmar y marcarNoRecibida, en una transacción porque acá no hay request HTTP
      transaccion.withTransaction(() -> {
        Entrega entrega = rutasRepository.buscarEntregaPorDonacionId(donacionId)
            .orElseThrow(() -> new NoSuchElementException("No se encontró entrega para la donación: " + donacionId));
        Ruta ruta = rutasRepository.buscarRutaPorEntregaId(entrega.getId())
            .orElseThrow(() -> new IllegalStateException("Entrega sin ruta: " + entrega.getId()));

        entrega.justificar(request.getJustificacion());
        if (request.getExito()) {
          entrega.marcarEntregada();
          String comprobante = entrega.getComprobante() + ", " + LocalDateTime.now().format(FORMATO_FECHA) + ", " + ruta.getCamion().getPatente();
          notificadorEntregas.avisarEntregada(entrega,
              request.getJustificacion().isBlank() ? comprobante : comprobante + ". " + request.getJustificacion());
        } else {
          entrega.marcarNoRecibida();
          notificadorEntregas.avisarFallida(entrega);
        }
        ruta.completarSiTermino();
      });

      ReportarEntregaResponse response = ReportarEntregaResponse.newBuilder()
          .setExito(true)
          .setMensaje("Entrega " + donacionId + " actualizada correctamente en Logística")
          .build();

      responseObserver.onNext(response);
      responseObserver.onCompleted();
    } catch (Exception e) {
      ReportarEntregaResponse response = ReportarEntregaResponse.newBuilder()
          .setExito(false)
          .setMensaje("Error al actualizar la entrega: " + e.getMessage())
          .build();

      responseObserver.onNext(response);
      responseObserver.onCompleted();
    } finally {
      if (rutasRepository.perThreadEntityManagerAccess().isAttached()) {
        rutasRepository.perThreadEntityManagerAccess().dispose();
      }
    }
  }

  // CRUD de camiones de la pantalla del admin. Como en reportarEntrega, cada llamada
  // cierra su EntityManager: si no, el hilo de gRPC seguiría leyendo datos viejos
  @Override
  public void obtenerCamiones(CamionesRequest request, StreamObserver<CamionesResponse> responseObserver) {
    try {
      List<DatosCamion> camiones = camionesRepository.obtenerTodos().stream()
          .map(LogisticaServiceImpl::datosCamion)
          .toList();
      responseObserver.onNext(CamionesResponse.newBuilder().addAllCamiones(camiones).build());
      responseObserver.onCompleted();
    } finally {
      camionesRepository.perThreadEntityManagerAccess().dispose();
    }
  }

  @Override
  public void obtenerCamion(CamionRequest request, StreamObserver<DatosCamion> responseObserver) {
    try {
      Camion camion = camionesRepository.buscarPorId(request.getId())
          .orElseThrow(() -> new NoSuchElementException("Camión inexistente"));
      responseObserver.onNext(datosCamion(camion));
      responseObserver.onCompleted();
    } catch (NoSuchElementException e) {
      responseObserver.onError(io.grpc.Status.NOT_FOUND.withDescription(e.getMessage()).asRuntimeException());
    } finally {
      camionesRepository.perThreadEntityManagerAccess().dispose();
    }
  }

  @Override
  public void crearCamion(DatosCamion request, StreamObserver<CamionResponse> responseObserver) {
    try {
      transaccion.withTransaction(() -> camionesRepository.agregar(
          new Camion(request.getPatente(), request.getVolumen(), request.getAltura(), request.getCargaMax())));
      responseObserver.onNext(CamionResponse.newBuilder().setExito(true)
          .setMensaje("Se agregó el camión " + request.getPatente() + ".").build());
    } catch (Exception e) {
      responseObserver.onNext(CamionResponse.newBuilder().setExito(false)
          .setMensaje("No se pudo agregar el camión: la patente " + request.getPatente() + " ya está cargada.").build());
    } finally {
      camionesRepository.perThreadEntityManagerAccess().dispose();
    }
    responseObserver.onCompleted();
  }

  @Override
  public void modificarCamion(DatosCamion request, StreamObserver<CamionResponse> responseObserver) {
    try {
      transaccion.withTransaction(() -> camionesRepository.buscarPorId(request.getId())
          .orElseThrow(() -> new NoSuchElementException("Camión inexistente"))
          .actualizarDatos(request.getPatente(), request.getVolumen(), request.getAltura(), request.getCargaMax()));
      responseObserver.onNext(CamionResponse.newBuilder().setExito(true)
          .setMensaje("Se guardaron los cambios del camión " + request.getPatente() + ".").build());
    } catch (Exception e) {
      responseObserver.onNext(CamionResponse.newBuilder().setExito(false)
          .setMensaje("No se pudieron guardar los cambios: la patente " + request.getPatente() + " ya está cargada.").build());
    } finally {
      camionesRepository.perThreadEntityManagerAccess().dispose();
    }
    responseObserver.onCompleted();
  }

  @Override
  public void eliminarCamion(CamionRequest request, StreamObserver<CamionResponse> responseObserver) {
    try {
      transaccion.withTransaction(() -> camionesRepository.eliminarPorId(request.getId()));
      responseObserver.onNext(CamionResponse.newBuilder().setExito(true)
          .setMensaje("Se eliminó el camión.").build());
    } catch (Exception e) {
      responseObserver.onNext(CamionResponse.newBuilder().setExito(false)
          .setMensaje("No se pudo eliminar el camión: ya tiene rutas registradas.").build());
    } finally {
      camionesRepository.perThreadEntityManagerAccess().dispose();
    }
    responseObserver.onCompleted();
  }

  // El camión como lo muestra la tabla del admin en donaciones
  private static DatosCamion datosCamion(Camion camion) {
    return DatosCamion.newBuilder()
        .setId(camion.getId())
        .setPatente(camion.getPatente())
        .setVolumen(camion.getVolumen())
        .setAltura(camion.getAltura())
        .setCargaMax(camion.getCargaMax())
        .setEstado(camion.getEstado().name())
        .setEstadoTexto(camion.getEstado().texto())
        .build();
  }
}
