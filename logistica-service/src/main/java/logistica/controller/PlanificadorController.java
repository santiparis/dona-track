package logistica.controller;

import io.javalin.http.Context;
import logistica.retrofit_client.PlanificacionCallbackRequest;
import logistica.domain.Ruta;
import logistica.service.PlanificadorService;

import java.io.IOException;
import java.util.List;
import java.util.NoSuchElementException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PlanificadorController {
  private static final Logger logger = LoggerFactory.getLogger(PlanificadorController.class);

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
    try {
      var body = ctx.bodyAsClass(PlanificacionCallbackRequest.class);
      List<Ruta> rutas = planificadorService.procesarPlanificacion(body);
      ctx.status(201).json(rutas);
    } catch (IllegalArgumentException e) {
      logger.warn("Callback de rutas invalido: {}", e.getMessage());
      ctx.status(400).json(new MensajeResponse(e.getMessage()));
    } catch (NoSuchElementException e) {
      logger.warn("Callback de rutas con referencia inexistente: {}", e.getMessage());
      ctx.status(404).json(new MensajeResponse(e.getMessage()));
    } catch (RuntimeException e) {
      logger.error("Error inesperado al procesar callback de rutas", e);
      ctx.status(500).json(new MensajeResponse("No se pudieron procesar las rutas"));
    }
  }
}
