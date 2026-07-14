package logistica.controller;

import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import logistica.domain.Camion;
import logistica.dto.CamionDTO;
import logistica.dto.LocalizacionDTO;
import logistica.service.CamionesService;

import java.util.NoSuchElementException;

public class CamionesController {
  private final CamionesService camionesService;

  public CamionesController(CamionesService camionesService) {
    this.camionesService = camionesService;
  }

  public void getCamiones(Context ctx) {
    ctx.json(camionesService.getCamiones());
  }

  public void postCamiones(Context ctx) {
    try {
      CamionDTO dto = ctx.bodyAsClass(CamionDTO.class);
      this.camionesService.postCamion(dto);
      ctx.status(HttpStatus.CREATED);
    } catch (RuntimeException e) {
      this.manejarExcepcion(ctx, e);
    }
  }

  public void putCamion(Context ctx) {
    try {
      String patente = ctx.pathParam("patente");
      CamionDTO dto = ctx.bodyAsClass(CamionDTO.class);
      Camion camionActualizado = this.camionesService.actualizarCamion(patente, dto);
      ctx.status(HttpStatus.OK).json(camionActualizado);
    } catch (RuntimeException e) {
      this.manejarExcepcion(ctx, e);
    }
  }

  public void deleteCamion(Context ctx) {
    String patente = ctx.pathParam("patente");

    try {
      this.camionesService.deleteCamion(patente);
      ctx.status(HttpStatus.NO_CONTENT);
    } catch (RuntimeException e) {
      this.manejarExcepcion(ctx, e);
    }
  }

  public void actualizarLocalizacion(Context ctx) {
    try {
      String patente = ctx.pathParam("patente");
      LocalizacionDTO dto = ctx.bodyAsClass(LocalizacionDTO.class);
      this.camionesService.actualizarLocalizacion(patente, dto);
      ctx.status(HttpStatus.OK);
    } catch (RuntimeException e) {
      this.manejarExcepcion(ctx, e);
    }
  }

  public record ErrorResponse(String mensaje) {}

  private void manejarExcepcion(Context ctx, RuntimeException e) {
    if (e instanceof IllegalArgumentException) {
      ctx.status(HttpStatus.BAD_REQUEST).json(new ErrorResponse(e.getMessage()));
      return;
    }

    if (e instanceof NoSuchElementException) {
      ctx.status(HttpStatus.NOT_FOUND).json(new ErrorResponse(e.getMessage()));
      return;
    }

    ctx.status(HttpStatus.INTERNAL_SERVER_ERROR).json(new ErrorResponse("Error inesperado"));
  }
}
