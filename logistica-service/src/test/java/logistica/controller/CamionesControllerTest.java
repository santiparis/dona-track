package logistica.controller;

import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import logistica.domain.Camion;
import logistica.dto.CamionDTO;
import logistica.dto.LocalizacionDTO;
import logistica.service.CamionesService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.NoSuchElementException;

import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class CamionesControllerTest {

  private CamionesService camionesService;
  private CamionesController controller;
  private Context ctx;

  @BeforeEach
  void setUp() {
    camionesService = mock(CamionesService.class);
    controller = new CamionesController(camionesService);
    ctx = mock(Context.class, RETURNS_DEEP_STUBS);
  }

  @Test
  void getCamionesDevuelveLaListaDelServicio() {
    var camiones = List.of(new Camion("AB123CD", 10, 2, 1000));
    when(camionesService.getCamiones()).thenReturn(camiones);

    controller.getCamiones(ctx);

    verify(ctx).json(camiones);
  }

  @Test
  void postCamionesDevuelveCreatedCuandoElServicioTerminaBien() {
    var dto = new CamionDTO("AB123CD", 10, 2, 1000);
    when(ctx.bodyAsClass(CamionDTO.class)).thenReturn(dto);

    controller.postCamiones(ctx);

    verify(camionesService).postCamion(dto);
    verify(ctx, atLeastOnce()).status(HttpStatus.CREATED);
  }

  @Test
  void putCamionDevuelveOkConElCamionActualizado() {
    var dto = new CamionDTO("AB123CD", 12, 2.5, 1500);
    var camionActualizado = new Camion("AB123CD", 12, 2.5, 1500);
    when(ctx.pathParam("id")).thenReturn("1");
    when(ctx.bodyAsClass(CamionDTO.class)).thenReturn(dto);
    when(camionesService.actualizarCamion(1L, dto)).thenReturn(camionActualizado);

    controller.putCamion(ctx);

    verify(camionesService).actualizarCamion(1L, dto);
    verify(ctx, atLeastOnce()).status(HttpStatus.OK);
  }

  @Test
  void putCamionDevuelveBadRequestCuandoLaPatenteNoCoincide() {
    var dto = new CamionDTO("XY987ZW", 12, 2.5, 1500);
    when(ctx.pathParam("id")).thenReturn("1");
    when(ctx.bodyAsClass(CamionDTO.class)).thenReturn(dto);
    doThrow(new IllegalArgumentException("La patente del camión debe coincidir con la de la ruta"))
        .when(camionesService).actualizarCamion(1L, dto);

    controller.putCamion(ctx);

    verify(ctx, atLeastOnce()).status(HttpStatus.BAD_REQUEST);
  }

  @Test
  void putCamionDevuelveNotFoundCuandoNoExiste() {
    var dto = new CamionDTO("AB123CD", 12, 2.5, 1500);
    when(ctx.pathParam("id")).thenReturn("1");
    when(ctx.bodyAsClass(CamionDTO.class)).thenReturn(dto);
    doThrow(new NoSuchElementException("Camión inexistente"))
        .when(camionesService).actualizarCamion(1L, dto);

    controller.putCamion(ctx);

    verify(ctx, atLeastOnce()).status(HttpStatus.NOT_FOUND);
  }

  @Test
  void deleteCamionDevuelveNoContentCuandoSeEliminaBien() {
    when(ctx.pathParam("id")).thenReturn("1");

    controller.deleteCamion(ctx);

    verify(camionesService).deleteCamion(1L);
    verify(ctx, atLeastOnce()).status(HttpStatus.NO_CONTENT);
  }

  @Test
  void deleteCamionDevuelveNotFoundCuandoNoExiste() {
    when(ctx.pathParam("id")).thenReturn("1");
    doThrow(new NoSuchElementException("Camión inexistente"))
        .when(camionesService).deleteCamion(1L);

    controller.deleteCamion(ctx);

    verify(ctx, atLeastOnce()).status(HttpStatus.NOT_FOUND);
  }

  @Test
  void actualizarLocalizacionDevuelveBadRequestSiLaCoordenadaEsInvalida() {
    var dto = new LocalizacionDTO(120, -58, 20);
    when(ctx.pathParam("id")).thenReturn("1");
    when(ctx.bodyAsClass(LocalizacionDTO.class)).thenReturn(dto);
    doThrow(new IllegalArgumentException("Latitud inválida"))
        .when(camionesService).actualizarLocalizacion(1L, dto);

    controller.actualizarLocalizacion(ctx);

    verify(ctx, atLeastOnce()).status(HttpStatus.BAD_REQUEST);
  }
}
