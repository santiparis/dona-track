package logistica.controller;

import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import logistica.NotificadorEntregas;
import logistica.domain.Entrega;
import logistica.repository.RutasRepository;

import java.util.NoSuchElementException;

public class EntregasController {

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
    cambiarEstadoEntrega(ctx, true);
  }

  public void marcarNoRecibida(Context ctx) {
    cambiarEstadoEntrega(ctx, false);
  }

  public void reingresarADeposito(Context ctx) {
    try {
      Long id = Long.parseLong(ctx.pathParam("id"));
      Entrega entrega = rutasRepository.buscarEntregaPorId(id)
          .orElseThrow(() -> new NoSuchElementException("Entrega no encontrada: " + id));
      entrega.reingresarADeposito();
      ctx.json(entrega);
    } catch (NumberFormatException e) {
      ctx.status(HttpStatus.BAD_REQUEST).json(new ErrorResponse("ID inválido"));
    } catch (NoSuchElementException e) {
      ctx.status(HttpStatus.NOT_FOUND).json(new ErrorResponse(e.getMessage()));
    } catch (IllegalStateException e) {
      ctx.status(HttpStatus.CONFLICT).json(new ErrorResponse(e.getMessage()));
    }
  }

  private void cambiarEstadoEntrega(Context ctx, boolean entregada) {
    try {
      Long id = Long.parseLong(ctx.pathParam("id"));
      Entrega entrega = entregada
          ? notificadorEntregas.confirmarEntrega(id)
          : notificadorEntregas.marcarNoRecibida(id);
      ctx.json(entrega);
    } catch (NumberFormatException e) {
      ctx.status(HttpStatus.BAD_REQUEST).json(new ErrorResponse("ID inválido"));
    } catch (NoSuchElementException e) {
      ctx.status(HttpStatus.NOT_FOUND).json(new ErrorResponse(e.getMessage()));
    } catch (IllegalStateException e) {
      ctx.status(HttpStatus.CONFLICT).json(new ErrorResponse(e.getMessage()));
    }
  }
}