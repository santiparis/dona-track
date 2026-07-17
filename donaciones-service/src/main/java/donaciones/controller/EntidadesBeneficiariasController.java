package donaciones.controller;

import donaciones.dto.EntidadBeneficiariaDTO;
import donaciones.dto.NecesidadDTO;
import donaciones.service.EntidadBeneficiariaService;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EntidadesBeneficiariasController {
  private static final Logger logger = LoggerFactory.getLogger(EntidadesBeneficiariasController.class);
  private final EntidadBeneficiariaService entidadesService;

  public EntidadesBeneficiariasController(EntidadBeneficiariaService entidadesService) {
    this.entidadesService = entidadesService;
  }

  public void getEntidadesBeneficiarias(Context ctx) {
    try {
      ctx.json(entidadesService.getEntidadesBeneficiarias());
    } catch (RuntimeException e) {
      logger.error("Error al listar entidades beneficiarias", e);
      ctx.status(HttpStatus.INTERNAL_SERVER_ERROR).result("Error al listar entidades beneficiarias: " + e.getMessage());
    }
  }

  public void postEntidadBeneficiaria(Context ctx) {
    try {
      EntidadBeneficiariaDTO dto = ctx.bodyAsClass(EntidadBeneficiariaDTO.class);
      entidadesService.postEntidadBeneficiaria(dto);
      ctx.status(HttpStatus.CREATED).result("Entidad Beneficiaria recibida y guardada");
    } catch (IllegalArgumentException e) {
      logger.warn("Error de validacion al crear entidad beneficiaria: {}", e.getMessage());
      ctx.status(HttpStatus.BAD_REQUEST).result("Error: " + e.getMessage());
    } catch (RuntimeException e) {
      logger.error("Error inesperado al crear entidad beneficiaria", e);
      ctx.status(HttpStatus.INTERNAL_SERVER_ERROR).result("Error al crear entidad beneficiaria: " + e.getMessage());
    }
  }

  public void putEntidadBeneficiaria(Context ctx) {
    try {
      Long id = Long.parseLong(ctx.pathParam("id"));
      EntidadBeneficiariaDTO dto = ctx.bodyAsClass(EntidadBeneficiariaDTO.class);
      entidadesService.putEntidadBeneficiaria(id, dto);
      ctx.result("Entidad beneficiaria actualizada");
    } catch (IllegalArgumentException e) {
      logger.warn("Error al actualizar entidad beneficiaria: {}", e.getMessage());
      ctx.status(HttpStatus.NOT_FOUND).result(e.getMessage());
    } catch (RuntimeException e) {
      logger.error("Error inesperado al actualizar entidad beneficiaria", e);
      ctx.status(HttpStatus.INTERNAL_SERVER_ERROR).result("Error al actualizar entidad beneficiaria: " + e.getMessage());
    }
  }

  public void patchEntidadBeneficiaria(Context ctx) {
    try {
      Long id = Long.parseLong(ctx.pathParam("id"));
      donaciones.dto.EntidadBeneficiariaPatchDTO dto = ctx.bodyAsClass(donaciones.dto.EntidadBeneficiariaPatchDTO.class);
      entidadesService.patchEntidadBeneficiaria(id, dto);
      ctx.result("Entidad beneficiaria actualizada parcialmente");
    } catch (IllegalArgumentException e) {
      logger.warn("Error al actualizar parcialmente entidad beneficiaria: {}", e.getMessage());
      ctx.status(HttpStatus.NOT_FOUND).result(e.getMessage());
    } catch (RuntimeException e) {
      logger.error("Error inesperado al actualizar parcialmente entidad beneficiaria", e);
      ctx.status(HttpStatus.INTERNAL_SERVER_ERROR).result("Error al actualizar entidad beneficiaria: " + e.getMessage());
    }
  }

  public void deleteEntidadBeneficiaria(Context ctx) {
    try {
      Long id = Long.parseLong(ctx.pathParam("id"));
      entidadesService.deleteEntidadBeneficiaria(id);
      ctx.result("Entidad beneficiaria eliminada correctamente");
    } catch (IllegalArgumentException e) {
      logger.warn("Error al eliminar entidad beneficiaria: {}", e.getMessage());
      ctx.status(HttpStatus.NOT_FOUND).result(e.getMessage());
    } catch (RuntimeException e) {
      logger.error("Error inesperado al eliminar entidad beneficiaria", e);
      ctx.status(HttpStatus.INTERNAL_SERVER_ERROR).result("Error al eliminar entidad beneficiaria: " + e.getMessage());
    }
  }

  public void getNecesidades(Context ctx) {
    try {
      Long id = Long.parseLong(ctx.pathParam("id"));
      ctx.json(entidadesService.getNecesidades(id));
    } catch (RuntimeException e) {
      logger.error("Error al listar necesidades", e);
      manejarExcepcion(ctx, e);
    }
  }

  public void postNecesidades(Context ctx) {
    Long id = Long.parseLong(ctx.pathParam("id"));
    NecesidadDTO dto = ctx.bodyAsClass(NecesidadDTO.class);

    try {
      entidadesService.postNecesidad(id, dto);
      ctx.status(HttpStatus.CREATED).result("Necesidad guardada correctamente");
    } catch (RuntimeException e) {
      manejarExcepcion(ctx, e);
    }
  }

  public void putNecesidad(Context ctx) {
    Long id = Long.parseLong(ctx.pathParam("id"));
    Long idNecesidad = Long.parseLong(ctx.pathParam("idNecesidad"));
    NecesidadDTO dto = ctx.bodyAsClass(NecesidadDTO.class);

    try {
      entidadesService.putNecesidad(id, idNecesidad, dto);
      ctx.result("Necesidad actualizada correctamente");
    } catch (RuntimeException e) {
      manejarExcepcion(ctx, e);
    }
  }

  public void patchNecesidad(Context ctx) {
    Long idEntidad = Long.parseLong(ctx.pathParam("id"));
    Long idNecesidad = Long.parseLong(ctx.pathParam("idNecesidad"));
    NecesidadDTO dto = ctx.bodyAsClass(NecesidadDTO.class);

    try {
      entidadesService.patchNecesidad(idEntidad, idNecesidad, dto);
      ctx.result("Necesidad actualizada parcialmente");
    } catch (RuntimeException e) {
      manejarExcepcion(ctx, e);
    }
  }

  public void deleteNecesidad(Context ctx) {
    Long id = Long.parseLong(ctx.pathParam("id"));
    Long idNecesidad = Long.parseLong(ctx.pathParam("idNecesidad"));

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
