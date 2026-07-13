package donaciones.controller;

import donaciones.service.DonacionService;
import donaciones.dto.DonacionRequestDTO;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;

public class DonacionController {

  private final DonacionService donacionService;

  public DonacionController(DonacionService service) {
    this.donacionService = service;
  }

  public void listar(Context ctx) {
    ctx.json(donacionService.listarDonaciones());
  }

  public void crear(Context ctx) {
    DonacionRequestDTO dto = ctx.bodyAsClass(DonacionRequestDTO.class);

    donacionService.crearDonacion(dto);

    ctx.status(HttpStatus.CREATED).result("Donación recibida y guardada");
  }

  public void cambiarEstado(Context ctx) {
    int id = Integer.parseInt(ctx.pathParam("id"));
    String nuevoEstadoTexto = ctx.queryParam("nuevo");

    try {
      donacionService.cambiarEstado(id, nuevoEstadoTexto);
      ctx.result("Estado actualizado");
    } catch (IllegalArgumentException e) {
      ctx.status(HttpStatus.BAD_REQUEST).result("Error: " + e.getMessage());
    }
  }

  public void eliminar(Context ctx) {
    int id = Integer.parseInt(ctx.pathParam("id"));

    try {
      donacionService.eliminarDonacion(id);
      ctx.result("Donacion eliminada");
    } catch (IllegalArgumentException e) {
      ctx.status(HttpStatus.NOT_FOUND).result(e.getMessage());
    }
  }
}