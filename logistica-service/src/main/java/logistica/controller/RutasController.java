package logistica.controller;

import io.javalin.http.Context;
import logistica.service.EntregasService;

import java.io.IOException;
import java.util.NoSuchElementException;

public class RutasController {

  private final EntregasService entregasService;

  public RutasController(EntregasService entregasService) {
    this.entregasService = entregasService;
  }

  public record ErrorResponse(String mensaje) {}

  public void obtenerRutas(Context ctx) {
    ctx.json(entregasService.listarRutas());
  }

  public void obtenerRuta(Context ctx) {
    String id = ctx.pathParam("id");
    entregasService.buscarRutaPorId(id).ifPresentOrElse(
        ctx::json,
        () -> ctx.status(404).json(new ErrorResponse("Ruta no encontrada: " + id))
    );
  }

  public void iniciarRuta(Context ctx) {
    String id = ctx.pathParam("id");
    try {
      var ruta = entregasService.iniciarRuta(id);
      ctx.json(ruta);
    } catch (NoSuchElementException e) {
      ctx.status(404).json(new ErrorResponse(e.getMessage()));
    } catch (IllegalStateException e) {
      ctx.status(409).json(new ErrorResponse(e.getMessage()));
    } catch (IOException e) {
      ctx.status(502).json(new ErrorResponse("No se pudo notificar a donaciones-service"));
    }
  }
}
