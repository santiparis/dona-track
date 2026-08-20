package donaciones.controller;

import donaciones.dto.AsignacionRequestDTO;
import donaciones.service.AsignacionService;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AsignacionController {
  private static final Logger logger = LoggerFactory.getLogger(AsignacionController.class);

  private final AsignacionService asignacionService;

  public AsignacionController(AsignacionService asignacionService) {
    this.asignacionService = asignacionService;
  }

  public void obtenerRanking(Context ctx) {
    try {
      Long id = Long.parseLong(ctx.pathParam("id"));
      String criterio = ctx.queryParamAsClass("criterio", String.class).getOrDefault("semantica");
      var ranking = asignacionService.ejecutarAlgoritmoYObtenerRanking(id, criterio);
      ctx.json(ranking);
    } catch (IllegalArgumentException e) {
      logger.warn("Error al obtener ranking de asignacion: {}", e.getMessage());
      ctx.status(HttpStatus.NOT_FOUND).result(e.getMessage());
    } catch (RuntimeException e) {
      logger.error("Error inesperado al obtener ranking de asignacion", e);
      ctx.status(HttpStatus.INTERNAL_SERVER_ERROR).result("Error al obtener ranking: " + e.getMessage());
    }
  }

  public void seleccionarEntidad(Context ctx) {
    try {
      Long id = Long.parseLong(ctx.pathParam("id"));
      Long idEntidad = Long.parseLong(ctx.pathParam("idEntidad"));
      AsignacionRequestDTO dto = ctx.bodyAsClass(AsignacionRequestDTO.class);
      asignacionService.confirmarAsignacion(id, idEntidad, dto.nombreEntidadSeleccionada());
      ctx.status(HttpStatus.CREATED).result("Asignacion confirmada y estado actualizado");
    } catch (IllegalArgumentException e) {
      logger.warn("Error al confirmar asignacion: {}", e.getMessage());
      ctx.status(HttpStatus.BAD_REQUEST).result(e.getMessage());
    } catch (RuntimeException e) {
      logger.error("Error inesperado al confirmar asignacion", e);
      ctx.status(HttpStatus.INTERNAL_SERVER_ERROR).result("Error al confirmar asignacion: " + e.getMessage());
    }
  }
}
