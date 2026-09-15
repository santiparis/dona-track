package logistica.controller;

import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import logistica.domain.Camion;
import logistica.dto.CamionDTO;
import logistica.dto.LocalizacionDTO;
import logistica.repository.CamionesRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class CamionesControllerTest {

  private CamionesRepository camionesRepository;
  private CamionesController controller;
  private Context ctx;
  private Camion camion;
  private Long camionId;

  @BeforeEach
  void setUp() {
    camionesRepository = new CamionesRepository();
    controller = new CamionesController(camionesRepository);
    ctx = mock(Context.class, RETURNS_DEEP_STUBS);

    camion = new Camion("AB123CD", 10, 2, 1000);
    camionesRepository.agregar(camion);
    // el id lo asigna el repositorio con una secuencia estatica, no se puede asumir que es 1
    camionId = camion.getId();
  }

  @Test
  void getCamionesDevuelveLosDelRepositorio() {
    controller.getCamiones(ctx);

    verify(ctx).json(camionesRepository.obtenerTodos());
  }

  @Test
  void postCamionesGuardaElCamionYDevuelveCreated() {
    when(ctx.bodyAsClass(CamionDTO.class)).thenReturn(new CamionDTO("XY987ZW", 12, 2.5, 1500));

    controller.postCamiones(ctx);

    verify(ctx, atLeastOnce()).status(HttpStatus.CREATED);
    assertTrue(camionesRepository.buscarPorPatente("XY987ZW").isPresent());
  }

  @Test
  void getCamionDevuelveNotFoundCuandoNoExiste() {
    when(ctx.pathParam("id")).thenReturn("999999");

    controller.getCamion(ctx);

    verify(ctx, atLeastOnce()).status(HttpStatus.NOT_FOUND);
  }

  @Test
  void getCamionDevuelveBadRequestSiElIdNoEsNumerico() {
    when(ctx.pathParam("id")).thenReturn("cam-1");

    controller.getCamion(ctx);

    verify(ctx, atLeastOnce()).status(HttpStatus.BAD_REQUEST);
  }

  @Test
  void putCamionActualizaLosDatosDelCamion() {
    when(ctx.pathParam("id")).thenReturn(String.valueOf(camionId));
    when(ctx.bodyAsClass(CamionDTO.class)).thenReturn(new CamionDTO("XY987ZW", 12, 2.5, 1500));

    controller.putCamion(ctx);

    verify(ctx, atLeastOnce()).status(HttpStatus.OK);
    Camion actualizado = camionesRepository.buscarPorId(camionId).orElseThrow();
    assertEquals("XY987ZW", actualizado.getPatente());
    assertEquals(12, actualizado.getVolumen());
    assertEquals(2.5, actualizado.getAltura());
    assertEquals(1500, actualizado.getCargaMax());
  }

  @Test
  void putCamionDevuelveNotFoundCuandoNoExiste() {
    when(ctx.pathParam("id")).thenReturn("999999");
    when(ctx.bodyAsClass(CamionDTO.class)).thenReturn(new CamionDTO("XY987ZW", 12, 2.5, 1500));

    controller.putCamion(ctx);

    verify(ctx, atLeastOnce()).status(HttpStatus.NOT_FOUND);
  }

  @Test
  void deleteCamionEliminaElCamionYDevuelveNoContent() {
    when(ctx.pathParam("id")).thenReturn(String.valueOf(camionId));

    controller.deleteCamion(ctx);

    verify(ctx, atLeastOnce()).status(HttpStatus.NO_CONTENT);
    assertTrue(camionesRepository.buscarPorId(camionId).isEmpty());
  }

  @Test
  void deleteCamionDevuelveNotFoundCuandoNoExiste() {
    when(ctx.pathParam("id")).thenReturn("999999");

    controller.deleteCamion(ctx);

    verify(ctx, atLeastOnce()).status(HttpStatus.NOT_FOUND);
  }

  @Test
  void actualizarLocalizacionGuardaCoordenadasYVelocidad() {
    when(ctx.pathParam("id")).thenReturn(String.valueOf(camionId));
    when(ctx.bodyAsClass(LocalizacionDTO.class)).thenReturn(new LocalizacionDTO(-34.60, -58.42, 47.5));

    controller.actualizarLocalizacion(ctx);

    verify(ctx, atLeastOnce()).status(HttpStatus.OK);
    assertEquals(-34.60, camion.getLocalizacion().getLatitud());
    assertEquals(-58.42, camion.getLocalizacion().getLongitud());
    assertEquals(47.5, camion.getVelocidad());
  }

  @Test
  void actualizarLocalizacionDevuelveBadRequestSiLaCoordenadaEsInvalida() {
    when(ctx.pathParam("id")).thenReturn(String.valueOf(camionId));
    when(ctx.bodyAsClass(LocalizacionDTO.class)).thenReturn(new LocalizacionDTO(120, -58.42, 20));

    controller.actualizarLocalizacion(ctx);

    verify(ctx, atLeastOnce()).status(HttpStatus.BAD_REQUEST);
    assertNull(camion.getLocalizacion());
  }

  @Test
  void actualizarLocalizacionDevuelveBadRequestSiLaVelocidadEsNegativa() {
    when(ctx.pathParam("id")).thenReturn(String.valueOf(camionId));
    when(ctx.bodyAsClass(LocalizacionDTO.class)).thenReturn(new LocalizacionDTO(-34.60, -58.42, -5));

    controller.actualizarLocalizacion(ctx);

    verify(ctx, atLeastOnce()).status(HttpStatus.BAD_REQUEST);
    // el camion no debe quedar con la localizacion nueva si la velocidad era invalida
    assertNull(camion.getLocalizacion());
  }

  @Test
  void actualizarLocalizacionDevuelveNotFoundSiElCamionNoExiste() {
    when(ctx.pathParam("id")).thenReturn("999999");
    when(ctx.bodyAsClass(LocalizacionDTO.class)).thenReturn(new LocalizacionDTO(-34.60, -58.42, 20));

    controller.actualizarLocalizacion(ctx);

    verify(ctx, atLeastOnce()).status(HttpStatus.NOT_FOUND);
  }
}
