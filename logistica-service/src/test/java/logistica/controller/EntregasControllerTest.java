package logistica.controller;

import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import logistica.domain.Entrega;
import logistica.notificacion.NotificadorEntregas;
import logistica.repository.RutasRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class EntregasControllerTest {

  private RutasRepository rutasRepository;
  private NotificadorEntregas notificadorEntregas;
  private EntregasController controller;
  private Context ctx;

  @BeforeEach
  void setUp() {
    rutasRepository = mock(RutasRepository.class);
    notificadorEntregas = mock(NotificadorEntregas.class);
    controller = new EntregasController(rutasRepository, notificadorEntregas);
    ctx = mock(Context.class, RETURNS_DEEP_STUBS);
  }

  @Test
  void marcarNoRecibidaDevuelveConflictSiLaEntregaNoEstaEnTraslado() {
    when(ctx.pathParam("id")).thenReturn("1");
    doThrow(new IllegalStateException("No se puede marcar no recibida desde PENDIENTE"))
        .when(notificadorEntregas).marcarNoRecibida(1L);

    controller.marcarNoRecibida(ctx);

    verify(ctx).status(HttpStatus.CONFLICT);
  }

  @Test
  void confirmarDevuelveNotFoundSiLaEntregaNoExiste() {
    when(ctx.pathParam("id")).thenReturn("1");
    doThrow(new NoSuchElementException("Entrega no encontrada: 1"))
        .when(notificadorEntregas).confirmarEntrega(1L);

    controller.confirmar(ctx);

    verify(ctx).status(HttpStatus.NOT_FOUND);
  }

  @Test
  void confirmarDevuelveBadRequestSiElIdNoEsNumerico() {
    when(ctx.pathParam("id")).thenReturn("ent-1");

    controller.confirmar(ctx);

    verify(ctx).status(HttpStatus.BAD_REQUEST);
  }

  @Test
  void obtenerEntregaDevuelveLaEntregaCuandoExiste() {
    Entrega entrega = entregaPendiente();
    when(ctx.pathParam("id")).thenReturn("1");
    when(rutasRepository.buscarEntregaPorId(1L)).thenReturn(Optional.of(entrega));

    controller.obtenerEntrega(ctx);

    verify(ctx).json(entrega);
  }

  @Test
  void reingresarADepositoDevuelveLaEntregaYaReingresada() {
    Entrega entrega = entregaNoRecibida();
    when(ctx.pathParam("id")).thenReturn("1");
    when(rutasRepository.buscarEntregaPorId(1L)).thenReturn(Optional.of(entrega));

    controller.reingresarADeposito(ctx);

    verify(ctx).json(entrega);
  }

  @Test
  void reingresarADepositoDevuelveNotFoundSiLaEntregaNoExiste() {
    when(ctx.pathParam("id")).thenReturn("1");
    when(rutasRepository.buscarEntregaPorId(1L)).thenReturn(Optional.empty());

    controller.reingresarADeposito(ctx);

    verify(ctx).status(HttpStatus.NOT_FOUND);
  }

  @Test
  void reingresarADepositoDevuelveConflictSiLaEntregaNoFueRechazada() {
    when(ctx.pathParam("id")).thenReturn("1");
    when(rutasRepository.buscarEntregaPorId(1L)).thenReturn(Optional.of(entregaPendiente()));

    controller.reingresarADeposito(ctx);

    verify(ctx).status(HttpStatus.CONFLICT);
  }

  private Entrega entregaPendiente() {
    return new Entrega(List.of(), "Calle 123", "Comedor Sol");
  }

  private Entrega entregaNoRecibida() {
    Entrega entrega = entregaPendiente();
    entrega.iniciarTraslado();
    entrega.marcarNoRecibida();
    return entrega;
  }
}
