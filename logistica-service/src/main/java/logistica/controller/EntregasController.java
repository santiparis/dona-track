package logistica.controller;

import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import logistica.domain.Entrega;
import logistica.notificacion.NotificadorEntregas;
import logistica.repository.RutasRepository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.NoSuchElementException;

public class EntregasController {


  private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

  private final RutasRepository rutasRepository;
  private final NotificadorEntregas notificadorEntregas;

  public EntregasController(RutasRepository rutasRepository, NotificadorEntregas notificadorEntregas) {
    this.rutasRepository = rutasRepository;
    this.notificadorEntregas = notificadorEntregas;
  }

  public record ErrorResponse(String mensaje) { }

  public void obtenerEntrega(Context ctx) {
    try {
      Long id = Long.parseLong(ctx.pathParam("id"));
      rutasRepository.buscarEntregaPorId(id).ifPresentOrElse(
          ctx::json,
          () -> ctx.status(404).json(new ErrorResponse("Entrega no encontrada: " + id))
      );
    } catch (NumberFormatException e) {
      ctx.status(400).json(new ErrorResponse("ID inválido"));
    }
  }

  public void confirmar(Context ctx) {
    try {
      Long entregaID = Long.parseLong(ctx.pathParam("id"));
      Entrega entrega = this.buscarEntrega(entregaID);

      // la entrega decide si la transicion es valida; recien despues se avisa afuera
      entrega.marcarEntregada();
      notificadorEntregas.avisarEntregada(entrega,
          entrega.getComprobante() + ", " + LocalDateTime.now().format(FORMATO_FECHA) + ", " + this.patenteDelCamion(entregaID));

      ctx.json(entrega);
    } catch (RuntimeException e) {
      this.manejarExcepcion(ctx, e);
    }
  }

  public void marcarNoRecibida(Context ctx) {
    try {
      Long entregaID = Long.parseLong(ctx.pathParam("id"));
      Entrega entrega = this.buscarEntrega(entregaID);

      entrega.marcarNoRecibida();
      notificadorEntregas.avisarFallida(entrega);

      ctx.json(entrega);
    } catch (RuntimeException e) {
      this.manejarExcepcion(ctx, e);
    }
  }

  // reingresar no le interesa a donaciones: la donacion sigue en poder de logistica
  public void reingresarADeposito(Context ctx) {
    try {
      Long entregaID = Long.parseLong(ctx.pathParam("id"));
      Entrega entrega = this.buscarEntrega(entregaID);

      entrega.reingresarADeposito();

      ctx.json(entrega);
    } catch (RuntimeException e) {
      this.manejarExcepcion(ctx, e);
    }
  }

  private Entrega buscarEntrega(Long entregaID) {
    return rutasRepository.buscarEntregaPorId(entregaID)
        .orElseThrow(() -> new NoSuchElementException("Entrega no encontrada: " + entregaID));
  }

  private String patenteDelCamion(Long entregaId) {
    return rutasRepository.buscarRutaPorEntregaId(entregaId)
        .map(ruta -> ruta.getCamion().getPatente())
        .orElse(null);
  }

  private void manejarExcepcion(Context ctx, RuntimeException e) {
    if (e instanceof NumberFormatException) {
      ctx.status(HttpStatus.BAD_REQUEST).json(new ErrorResponse("ID inválido"));
      return;
    }

    if (e instanceof NoSuchElementException) {
      ctx.status(HttpStatus.NOT_FOUND).json(new ErrorResponse(e.getMessage()));
      return;
    }

    if (e instanceof IllegalStateException) {
      ctx.status(HttpStatus.CONFLICT).json(new ErrorResponse(e.getMessage()));
      return;
    }

    ctx.status(HttpStatus.INTERNAL_SERVER_ERROR).json(new ErrorResponse("Error inesperado"));
  }
}
