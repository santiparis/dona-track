package logistica.controller;

import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import logistica.domain.Entrega;
import logistica.service.EntregasService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.NoSuchElementException;

import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class EntregasControllerTest {

  private EntregasService entregasService;
  private EntregasController controller;
  private Context ctx;

  @BeforeEach
  void setUp() {
    entregasService = mock(EntregasService.class);
    controller = new EntregasController(entregasService);
    ctx = mock(Context.class, RETURNS_DEEP_STUBS);
  }

  @Test
  void marcarNoRecibidaDevuelveConflictSiLaEntregaNoEstaEnTraslado() throws IOException {
    when(ctx.pathParam("id")).thenReturn("ent-1");
    doThrow(new IllegalStateException("No se puede marcar no recibida desde PENDIENTE"))
        .when(entregasService).marcarNoRecibida("ent-1");

    controller.marcarNoRecibida(ctx);

    verify(ctx).status(HttpStatus.CONFLICT);
  }

  @Test
  void confirmarDevuelveNotFoundSiLaEntregaNoExiste() throws IOException {
    when(ctx.pathParam("id")).thenReturn("ent-1");
    doThrow(new NoSuchElementException("Entrega no encontrada: ent-1"))
        .when(entregasService).confirmarEntrega("ent-1");

    controller.confirmar(ctx);

    verify(ctx).status(HttpStatus.NOT_FOUND);
  }
}
