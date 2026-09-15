package logistica.controller;

import io.javalin.http.Context;
import logistica.NotificadorEntregas;
import logistica.repository.RutasRepository;

import java.util.NoSuchElementException;


public class RutasController {

  private final RutasRepository rutasRepository;
  private final NotificadorEntregas notificadorEntregas;

  public RutasController(RutasRepository rutasRepository,
                         NotificadorEntregas notificadorEntregas) {
    this.rutasRepository = rutasRepository;
    this.notificadorEntregas = notificadorEntregas;
  }

  public record ErrorResponse(String mensaje) {}

  public void obtenerRutas(Context ctx) {
    ctx.json(rutasRepository.obtenerTodas());
  }

  public void obtenerRuta(Context ctx) {
    try {
      Long id = Long.parseLong(ctx.pathParam("id"));
      rutasRepository.buscarPorId(id).ifPresentOrElse(
          ctx::json,
          () -> ctx.status(404).json(new ErrorResponse("Ruta no encontrada: " + id))
      );
    } catch (NumberFormatException e) {
      ctx.status(400).json(new ErrorResponse("ID inválido"));
    }
  }

  public void iniciarRuta(Context ctx) {
    try {
      Long id = Long.parseLong(ctx.pathParam("id"));
      var ruta = notificadorEntregas.iniciarRuta(id);
      ctx.json(ruta);
    } catch (NumberFormatException e) {
      ctx.status(400).json(new ErrorResponse("ID inválido"));
    } catch (NoSuchElementException e) {
      ctx.status(404).json(new ErrorResponse(e.getMessage()));
    } catch (IllegalStateException e) {
      ctx.status(409).json(new ErrorResponse(e.getMessage()));
    }
  }
}
