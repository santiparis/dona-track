package logistica.handler;

import io.javalin.http.Context;
import logistica.domain.Coordenadas;
import logistica.domain.Entrega;
import logistica.domain.EstadoEntrega;
import logistica.domain.RepositorioEntregas;

public class DonacionesAPIHandler {
  private final RepositorioEntregas repositorio;

  public DonacionesAPIHandler(RepositorioEntregas repositorio) {
    this.repositorio = repositorio;
  }

  public record CrearEntregaRequest(String donacionId, double latitud, double longitud) {}
  public record ActualizarEstadoRequest(EstadoEntrega estado) {}

  public record ErrorResponse(String mensaje) {}

  public void crear(Context ctx) {
    var body = ctx.bodyAsClass(CrearEntregaRequest.class);
    var destino = new Coordenadas(body.latitud(), body.longitud());
    var entrega = new Entrega(body.donacionId(), destino);
    repositorio.agregar(entrega);
    ctx.status(201).json(entrega);
  }

  public void obtener(Context ctx) {
    var id = ctx.pathParam("id");
    repositorio.buscarPorId(id).ifPresentOrElse(
        ctx::json,
        () -> ctx.status(404).json(new ErrorResponse("Entrega no encontrada")));
  }

  public void actualizarEstado(Context ctx) {
    var id = ctx.pathParam("id");
    var body = ctx.bodyAsClass(ActualizarEstadoRequest.class);
    repositorio.buscarPorId(id).ifPresentOrElse(
        entrega -> {
          entrega.actualizarEstado(body.estado());
          ctx.json(entrega);
        },
        () -> ctx.status(404).json(new ErrorResponse("Entrega no encontrada")));
  }
}