package donaciones.controller;

import donaciones.dto.AsignacionRequestDTO;
import donaciones.service.AsignacionService;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;

public class AsignacionController {

  private final AsignacionService asignacionService;

  public AsignacionController(AsignacionService asignacionService) {
    this.asignacionService = asignacionService;
  }

  public void obtenerRanking(Context ctx) {
    int id = Integer.parseInt(ctx.pathParam("id"));
    String criterio = ctx.queryParamAsClass("criterio", String.class).getOrDefault("semantica");
    try {
      var ranking = asignacionService.ejecutarAlgoritmoYObtenerRanking(id, criterio);
      ctx.json(ranking);
    } catch (IllegalArgumentException e) {
      ctx.status(HttpStatus.NOT_FOUND).result(e.getMessage());
    }
  }

  public void seleccionarEntidad(Context ctx) {
    int id = Integer.parseInt(ctx.pathParam("id"));
    Long idEntidad = Long.parseLong(ctx.pathParam("idEntidad"));

    AsignacionRequestDTO dto = ctx.bodyAsClass(AsignacionRequestDTO.class);

    try {
      asignacionService.confirmarAsignacion(id, idEntidad, dto.nombreEntidadSeleccionada());
      ctx.status(HttpStatus.CREATED).result("Asignacion confirmada y estado actualizado");
    } catch (IllegalArgumentException e) {
      ctx.status(HttpStatus.BAD_REQUEST).result(e.getMessage());
    }
  }
}