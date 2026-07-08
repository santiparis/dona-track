package logistica.controller;

import io.javalin.http.Context;
import logistica.domain.Coordenadas;
import logistica.domain.Entrega;
import logistica.domain.EstadoEntrega;
import logistica.repository.RepositorioEntregas;
import logistica.service.DonacionesAPIService;

public class DonacionesAPIController {
  private final DonacionesAPIService apiService;

  public DonacionesAPIController(DonacionesAPIService service) {

    this.apiService = service;
  }

  public record CrearEntregaRequest(String donacionId, double latitud, double longitud) {}
  public record ActualizarEstadoRequest(EstadoEntrega estado) {}

  public record ErrorResponse(String mensaje) {}

  public void crear(Context ctx) {
    var body = ctx.bodyAsClass(CrearEntregaRequest.class);
    var destino = new Coordenadas(body.latitud(), body.longitud());
    var entrega = new Entrega(body.donacionId(), destino);
    apiService.agregar(entrega);
    ctx.status(201).json(entrega);
  }

  public void obtener(Context ctx) {
    var id = ctx.pathParam("id");
    apiService.buscarPorId(id).ifPresentOrElse(
        ctx::json,
        () -> ctx.status(404).json(new ErrorResponse("Entrega no encontrada")));
  }

  public void actualizarEstado(Context ctx) {
    var id = ctx.pathParam("id");
    var body = ctx.bodyAsClass(ActualizarEstadoRequest.class);
    apiService.buscarPorId(id).ifPresentOrElse(
        entrega -> {
          entrega.actualizarEstado(body.estado());
          ctx.json(entrega);
        },
        () -> ctx.status(404).json(new ErrorResponse("Entrega no encontrada")));
  }
}