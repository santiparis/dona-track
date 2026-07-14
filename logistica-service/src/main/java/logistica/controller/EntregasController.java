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
    String id = ctx.pathParam("id");
    entregasService.buscarPorId(id).ifPresentOrElse(
        ctx::json,
        () -> ctx.status(404).json(new ErrorResponse("Entrega no encontrada: " + id))
    );
  }

  public void confirmar(Context ctx) throws IOException {
    String id = ctx.pathParam("id");
    ctx.json(entregasService.confirmarEntrega(id));
  }

  public void marcarNoRecibida(Context ctx) throws IOException {
    String id = ctx.pathParam("id");
    ctx.json(entregasService.marcarNoRecibida(id));
  }

  public void reingresarADeposito(Context ctx) {
    String id = ctx.pathParam("id");
    ctx.json(entregasService.reingresarADeposito(id));
  }
}
