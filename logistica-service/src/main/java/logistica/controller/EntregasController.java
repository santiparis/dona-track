package logistica.controller;

import io.javalin.http.Context;
import logistica.service.EntregasService;

public class EntregasController {

  private final EntregasService entregasService;

  public EntregasController(EntregasService entregasService) {
    this.entregasService = entregasService;
  }

  public record ErrorResponse(String mensaje) {}

  public void confirmar(Context ctx) {
    String id = ctx.pathParam("id");
    ctx.json(entregasService.confirmarEntrega(id));
  }

  public void marcarNoRecibida(Context ctx) {
    String id = ctx.pathParam("id");
    ctx.json(entregasService.marcarNoRecibida(id));
  }

  public void reingresarADeposito(Context ctx) {
    String id = ctx.pathParam("id");
    ctx.json(entregasService.reingresarADeposito(id));
  }
}
