package donaciones.controller;

import donaciones.dto.EntidadBeneficiariaDTO;
import donaciones.dto.NecesidadDTO;
import donaciones.service.EntidadBeneficiariaService;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;

public class EntidadesBeneficiariasController {
  private final EntidadBeneficiariaService entidadesService;

  public EntidadesBeneficiariasController(EntidadBeneficiariaService entidadesService) {
    this.entidadesService = entidadesService;
  }

  public void getEntidadesBeneficiarias(Context ctx) {
    ctx.json(entidadesService.getEntidadesBeneficiarias());
  }

  public void postEntidadBeneficiaria(Context ctx) {
    EntidadBeneficiariaDTO dto = ctx.bodyAsClass(EntidadBeneficiariaDTO.class);
    try {
      entidadesService.postEntidadBeneficiaria(dto);
      ctx.status(HttpStatus.CREATED).result("Entidad Beneficiaria recibida y guardada");
    } catch (IllegalArgumentException e) {
      ctx.status(HttpStatus.BAD_REQUEST).result("Error: " + e.getMessage());
    }
  }

  public void putEntidadBeneficiaria(Context ctx) {
    int id = Integer.parseInt(ctx.pathParam("id"));
    EntidadBeneficiariaDTO dto = ctx.bodyAsClass(EntidadBeneficiariaDTO.class);

    try {
      entidadesService.putEntidadBeneficiaria(id, dto);
      ctx.result("Entidad beneficiaria actualizada");
    } catch (IllegalArgumentException e) {
      ctx.status(HttpStatus.NOT_FOUND).result(e.getMessage());
    }
  }

  public void patchEntidadBeneficiaria(Context ctx) {
    int id = Integer.parseInt(ctx.pathParam("id"));
    donaciones.dto.EntidadBeneficiariaPatchDTO dto = ctx.bodyAsClass(donaciones.dto.EntidadBeneficiariaPatchDTO.class);

    try {
      entidadesService.patchEntidadBeneficiaria(id, dto);
      ctx.result("Entidad beneficiaria actualizada parcialmente");
    } catch (IllegalArgumentException e) {
      ctx.status(HttpStatus.NOT_FOUND).result(e.getMessage());
    }
  }

  public void deleteEntidadBeneficiaria(Context ctx) {
    int id = Integer.parseInt(ctx.pathParam("id"));

    try {
      entidadesService.deleteEntidadBeneficiaria(id);
      ctx.result("Entidad beneficiaria eliminada correctamente");
    } catch (IllegalArgumentException e) {
      ctx.status(HttpStatus.NOT_FOUND).result(e.getMessage());
    }
  }

  public void getNecesidades(Context ctx) {
    int id = Integer.parseInt(ctx.pathParam("id"));
    ctx.json(entidadesService.getNecesidades(id));
  }

  public void postNecesidades(Context ctx) {
    int id = Integer.parseInt(ctx.pathParam("id"));
    NecesidadDTO dto = ctx.bodyAsClass(NecesidadDTO.class);

    try {
      entidadesService.postNecesidad(id, dto);
      ctx.status(HttpStatus.CREATED).result("Necesidad guardada correctamente");
    } catch (RuntimeException e) {
      manejarExcepcion(ctx, e);
    }
  }

  public void putNecesidad(Context ctx) {
    int id = Integer.parseInt(ctx.pathParam("id"));
    int idNecesidad = Integer.parseInt(ctx.pathParam("idNecesidad"));
    NecesidadDTO dto = ctx.bodyAsClass(NecesidadDTO.class);

    try {
      entidadesService.putNecesidad(id, idNecesidad, dto);
      ctx.result("Necesidad actualizada correctamente");
    } catch (RuntimeException e) {
      manejarExcepcion(ctx, e);
    }
  }

  public void patchNecesidad(Context ctx) {
    int idEntidad = Integer.parseInt(ctx.pathParam("id"));
    int idNecesidad = Integer.parseInt(ctx.pathParam("idNecesidad"));
    NecesidadDTO dto = ctx.bodyAsClass(NecesidadDTO.class);

    try {
      entidadesService.patchNecesidad(idEntidad, idNecesidad, dto);
      ctx.result("Necesidad actualizada parcialmente");
    } catch (RuntimeException e) {
      manejarExcepcion(ctx, e);
    }
  }

  public void deleteNecesidad(Context ctx) {
    int id = Integer.parseInt(ctx.pathParam("id"));
    int idNecesidad = Integer.parseInt(ctx.pathParam("idNecesidad"));

    try {
      entidadesService.deleteNecesidad(id, idNecesidad);
      ctx.result("Necesidad eliminada correctamente");
    } catch (RuntimeException e) {
      manejarExcepcion(ctx, e);
    }
  }

  private void manejarExcepcion(Context ctx, RuntimeException e) {
    if (e instanceof IllegalArgumentException) {
      ctx.status(HttpStatus.BAD_REQUEST).result("Error: " + e.getMessage());
      return;
    }

    ctx.status(HttpStatus.NOT_FOUND).result("Error: " + e.getMessage());
  }
}
