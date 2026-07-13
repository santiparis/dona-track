package logistica.controller;

import io.javalin.http.Context;
import logistica.retrofit_client.PlanificacionCallbackRequest;
import logistica.domain.Ruta;
import logistica.service.PlanificadorService;

import java.util.List;

public class PlanificadorController {

  private final PlanificadorService planificadorService;

  public PlanificadorController(PlanificadorService planificadorService) {
    this.planificadorService = planificadorService;
  }

  public void obtenerRutas(Context ctx) {
    var body = ctx.bodyAsClass(PlanificacionCallbackRequest.class);
    List<Ruta> rutas = planificadorService.procesarPlanificacion(body);
    ctx.status(201).json(rutas);
  }
}