package logistica.controller;

import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import logistica.service.EntregasService;

import java.io.IOException;
import java.util.NoSuchElementException;

public class EntregasController {

  private final EntregasService entregasService;

  public EntregasController(EntregasService entregasService) {
    this.entregasService = entregasService;
  }

  public record ErrorResponse(String mensaje) {}

  public void obtenerEntrega(Context ctx) {
    try {
      Long id = Long.parseLong(ctx.pathParam("id"));
      entregasService.buscarPorId(id).ifPresentOrElse(
          ctx::json,
          () -> ctx.status(404).json(new ErrorResponse("Entrega no encontrada: " + id))
      );
    } catch (NumberFormatException e) {
      ctx.status(400).json(new ErrorResponse("ID inválido"));
    }
  }

  public void confirmar(Context ctx) throws IOException {
    cambiarEstadoEntrega(ctx, true);
  }

  public void marcarNoRecibida(Context ctx) throws IOException {
    cambiarEstadoEntrega(ctx, false);
  }

  public void reingresarADeposito(Context ctx) {
    String id = ctx.pathParam("id");
    try {
      ctx.json(entregasService.reingresarADeposito(id));
    } catch (NoSuchElementException e) {
      ctx.status(HttpStatus.NOT_FOUND).json(new ErrorResponse(e.getMessage()));
    } catch (IllegalStateException e) {
      ctx.status(HttpStatus.CONFLICT).json(new ErrorResponse(e.getMessage()));
    }
  }

  private void cambiarEstadoEntrega(Context ctx, boolean entregada) throws IOException {
    String id = ctx.pathParam("id");
    try {
      if (entregada) {
        ctx.json(entregasService.confirmarEntrega(id));
      } else {
        ctx.json(entregasService.marcarNoRecibida(id));
      }
    } catch (NoSuchElementException e) {
      ctx.status(HttpStatus.NOT_FOUND).json(new ErrorResponse(e.getMessage()));
    } catch (IllegalStateException e) {
      ctx.status(HttpStatus.CONFLICT).json(new ErrorResponse(e.getMessage()));
    }
  }
}
