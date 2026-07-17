package logistica.controller;

import io.javalin.http.Context;
import logistica.service.EntregasService;

import java.io.IOException;

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
    try {
      Long id = Long.parseLong(ctx.pathParam("id"));
      ctx.json(entregasService.confirmarEntrega(id));
    } catch (NumberFormatException e) {
      ctx.status(400).json(new ErrorResponse("ID inválido"));
    }
  }

  public void marcarNoRecibida(Context ctx) throws IOException {
    try {
      Long id = Long.parseLong(ctx.pathParam("id"));
      ctx.json(entregasService.marcarNoRecibida(id));
    } catch (NumberFormatException e) {
      ctx.status(400).json(new ErrorResponse("ID inválido"));
    }
  }

  public void reingresarADeposito(Context ctx) {
    try {
      Long id = Long.parseLong(ctx.pathParam("id"));
      ctx.json(entregasService.reingresarADeposito(id));
    } catch (NumberFormatException e) {
      ctx.status(400).json(new ErrorResponse("ID inválido"));
    }
  }
}
