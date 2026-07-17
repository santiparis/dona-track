package donaciones.controller;

import donaciones.dto.DonanteRequestDTO;
import donaciones.service.DonanteService;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;

public class DonanteController {

  private final DonanteService donanteService;

  public DonanteController(DonanteService donanteService) {
    this.donanteService = donanteService;
  }

  public void listar(Context ctx) {
    ctx.json(donanteService.listarDonantes());
  }

  public void crear(Context ctx) {
    DonanteRequestDTO dto = ctx.bodyAsClass(DonanteRequestDTO.class);
    try {
      donanteService.crearDonante(dto);
      ctx.status(HttpStatus.CREATED).result("Donante registrado");
    } catch (IllegalArgumentException e) {
      ctx.status(HttpStatus.BAD_REQUEST).result(e.getMessage());
    }
  }

  public void actualizar(Context ctx) {
    Long id = Long.parseLong(ctx.pathParam("id"));
    DonanteRequestDTO dto = ctx.bodyAsClass(DonanteRequestDTO.class);

    try {
      donanteService.actualizarDonante(id, dto);
      ctx.result("Datos del donante actualizados");
    } catch (IllegalArgumentException e) {
      ctx.status(HttpStatus.NOT_FOUND).result(e.getMessage());
    }
  }

  public void eliminar(Context ctx) {
    Long id = Long.parseLong(ctx.pathParam("id"));
    donanteService.eliminarDonante(id);
    ctx.result("Donante eliminado");
  }
}
