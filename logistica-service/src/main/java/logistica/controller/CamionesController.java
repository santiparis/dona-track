package logistica.controller;

import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import logistica.domain.Camion;
import logistica.domain.Coordenadas;
import logistica.dto.CamionDTO;
import logistica.dto.LocalizacionDTO;
import logistica.repository.CamionesRepository;

import java.util.NoSuchElementException;

public class CamionesController {
  private final CamionesRepository camionesRepository;

  // Como no hay service ahora el controller conoce al repository
  public CamionesController(CamionesRepository camionesRepository) {
    this.camionesRepository = camionesRepository;
  }

  public void getCamiones(Context ctx) {
    ctx.json(camionesRepository.obtenerTodos());
  }

  public void postCamiones(Context ctx) {
    try {
      CamionDTO dto = ctx.bodyAsClass(CamionDTO.class);
      this.camionesRepository.agregar(new Camion(dto.patente(),
          dto.volumen(),
          dto.altura(),
          dto.cargaMax()
      ));
      ctx.status(HttpStatus.CREATED);
    } catch (RuntimeException e) {
      this.manejarExcepcion(ctx, e);
    }
  }

  public void getCamion(Context ctx) {
    try {
      Long id = Long.parseLong(ctx.pathParam("id"));
      Camion camion = camionesRepository.buscarPorId(id)
          .orElseThrow(() -> new NoSuchElementException("Camión inexistente"));
      ctx.json(camion);
    } catch (NumberFormatException e) {
      ctx.status(HttpStatus.BAD_REQUEST).json(new ErrorResponse("ID inválido"));
    } catch (NoSuchElementException e) {
      ctx.status(HttpStatus.NOT_FOUND).json(new ErrorResponse(e.getMessage()));
    }
  }

  public void putCamion(Context ctx) {
    try {
      Long id = Long.parseLong(ctx.pathParam("id"));
      CamionDTO dto = ctx.bodyAsClass(CamionDTO.class);

      Camion camion = this.camionesRepository.buscarPorId(id)
          .orElseThrow(() -> new NoSuchElementException("Camión inexistente"));

      camion.actualizarDatos(dto.patente(), dto.volumen(), dto.altura(), dto.cargaMax());

      ctx.status(HttpStatus.OK).json(camion);
    } catch (NumberFormatException e) {
      ctx.status(HttpStatus.BAD_REQUEST).json(new ErrorResponse("ID inválido"));
    } catch (RuntimeException e) {
      this.manejarExcepcion(ctx, e);
    }
  }

  public void deleteCamion(Context ctx) {
    try {
      Long id = Long.parseLong(ctx.pathParam("id"));
      camionesRepository.buscarPorId(id)
          .orElseThrow(() -> new NoSuchElementException("Camión inexistente"));
      camionesRepository.eliminarPorId(id);
      ctx.status(HttpStatus.NO_CONTENT);
    } catch (NumberFormatException e) {
      ctx.status(HttpStatus.BAD_REQUEST).json(new ErrorResponse("ID inválido"));
    } catch (RuntimeException e) {
      this.manejarExcepcion(ctx, e);
    }
  }

  public void actualizarLocalizacion(Context ctx) {
    try {
      Long id = Long.parseLong(ctx.pathParam("id"));
      LocalizacionDTO dto = ctx.bodyAsClass(LocalizacionDTO.class);

      Camion camion = camionesRepository.buscarPorId(id)
          .orElseThrow(() -> new NoSuchElementException("Camión inexistente"));
      camion.actualizarLocalizacion(new Coordenadas(dto.latitud(), dto.longitud()), dto.velocidad());

      ctx.status(HttpStatus.OK);
    } catch (NumberFormatException e) {
      ctx.status(HttpStatus.BAD_REQUEST).json(new ErrorResponse("ID inválido"));
    } catch (RuntimeException e) {
      this.manejarExcepcion(ctx, e);
    }
  }

  public record ErrorResponse(String mensaje) { }

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
