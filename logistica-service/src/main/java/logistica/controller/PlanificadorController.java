package logistica.controller;

import io.javalin.http.Context;
import logistica.retrofit_client.PlanificacionCallbackRequest;
import logistica.domain.Ruta;
import logistica.service.PlanificadorService;

import java.io.IOException;
import java.util.List;

public class PlanificadorController {

  private final PlanificadorService planificadorService;

  public PlanificadorController(PlanificadorService planificadorService) {
    this.planificadorService = planificadorService;
  }

  public record MensajeResponse(String mensaje) {}

  // lo dispara el cron (o a demanda): arranca la planificacion con los repos vivos del proceso
  public void planificar(Context ctx) {
    try {
      planificadorService.enviarPlanificacion();
      ctx.status(202).json(new MensajeResponse("Planificacion disparada"));
    } catch (IOException e) {
      ctx.status(502).json(new MensajeResponse("No se pudo contactar al planificador"));
    }
  }

  // callback: el planificador externo devuelve las rutas armadas
  public void obtenerRutas(Context ctx) {
    var body = ctx.bodyAsClass(PlanificacionCallbackRequest.class);
    List<Ruta> rutas = planificadorService.procesarPlanificacion(body);
    ctx.status(201).json(rutas);
  }
}