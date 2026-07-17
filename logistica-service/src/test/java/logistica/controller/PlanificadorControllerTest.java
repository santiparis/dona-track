package logistica.controller;

import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import logistica.domain.Camion;
import logistica.domain.Ruta;
import logistica.retrofit_client.PlanificacionCallbackRequest;
import logistica.service.PlanificadorService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.NoSuchElementException;

import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class PlanificadorControllerTest {

  private PlanificadorService planificadorService;
  private PlanificadorController controller;
  private Context ctx;

  @BeforeEach
  void setUp() {
    planificadorService = mock(PlanificadorService.class);
    controller = new PlanificadorController(planificadorService);
    ctx = mock(Context.class, RETURNS_DEEP_STUBS);
  }

  @Test
  void obtenerRutasDevuelveCreatedCuandoElCallbackEsValido() {
    var body = new PlanificacionCallbackRequest(List.of(), List.of());
    when(ctx.bodyAsClass(PlanificacionCallbackRequest.class)).thenReturn(body);
    when(planificadorService.procesarPlanificacion(body)).thenReturn(List.of(new Ruta(new Camion("AB123CD", 10, 2, 1000), List.of())));

    controller.obtenerRutas(ctx);

    verify(ctx).status(201);
  }

  @Test
  void obtenerRutasDevuelveBadRequestCuandoElCallbackEsInvalido() {
    when(ctx.bodyAsClass(PlanificacionCallbackRequest.class)).thenThrow(new IllegalArgumentException("Body invalido"));

    controller.obtenerRutas(ctx);

    verify(ctx).status(400);
  }

  @Test
  void obtenerRutasDevuelveNotFoundCuandoElCamionNoExiste() {
    var body = new PlanificacionCallbackRequest(List.of(), List.of());
    when(ctx.bodyAsClass(PlanificacionCallbackRequest.class)).thenReturn(body);
    doThrow(new NoSuchElementException("Camion no encontrado: AB123CD"))
        .when(planificadorService).procesarPlanificacion(body);

    controller.obtenerRutas(ctx);

    verify(ctx).status(404);
  }
}
