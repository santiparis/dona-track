package logistica.controller;

import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import logistica.domain.Camion;
import logistica.domain.Entrega;
import logistica.domain.EstadoEntrega;
import logistica.domain.Ruta;
import logistica.notificacion.NotificadorEntregas;
import logistica.repository.RutasRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
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
  void confirmarMarcaLaEntregaYAvisaConElComprobante() {
    Entrega entrega = entregaEnTraslado();
    when(ctx.pathParam("id")).thenReturn("1");
    when(rutasRepository.buscarEntregaPorId(1L)).thenReturn(Optional.of(entrega));
    when(rutasRepository.buscarRutaPorEntregaId(1L)).thenReturn(Optional.of(rutaCon(entrega)));

    controller.confirmar(ctx);

    assertEquals(EstadoEntrega.ENTREGADA, entrega.getEstado());

    // la fecha se genera al vuelo, asi que se verifica lo que si es estable: comprobante y patente
    ArgumentCaptor<String> comprobante = ArgumentCaptor.forClass(String.class);
    verify(notificadorEntregas).avisarEntregada(eq(entrega), comprobante.capture());
    assertTrue(comprobante.getValue().startsWith(entrega.getComprobante() + ", "));
    assertTrue(comprobante.getValue().endsWith(", AB123CD"));

    verify(ctx).json(entrega);
  }

  @Test
  void marcarNoRecibidaMarcaLaEntregaYAvisaLaFalla() {
    Entrega entrega = entregaEnTraslado();
    when(ctx.pathParam("id")).thenReturn("1");
    when(rutasRepository.buscarEntregaPorId(1L)).thenReturn(Optional.of(entrega));
    when(rutasRepository.buscarRutaPorEntregaId(1L)).thenReturn(Optional.of(rutaCon(entrega)));

    controller.marcarNoRecibida(ctx);

    assertEquals(EstadoEntrega.NO_RECIBIDA, entrega.getEstado());
    verify(notificadorEntregas).avisarFallida(entrega);
    verify(ctx).json(entrega);
  }

  @Test
  void marcarNoRecibidaDevuelveConflictSiLaEntregaNoEstaEnTraslado() {
    Entrega entrega = entregaPendiente();
    when(ctx.pathParam("id")).thenReturn("1");
    when(rutasRepository.buscarEntregaPorId(1L)).thenReturn(Optional.of(entrega));
    when(rutasRepository.buscarRutaPorEntregaId(1L)).thenReturn(Optional.of(rutaCon(entrega)));

    controller.marcarNoRecibida(ctx);

    verify(ctx).status(HttpStatus.CONFLICT);
    // si el dominio rechaza la transicion, no se le avisa a donaciones
    verifyNoInteractions(notificadorEntregas);
  }

  @Test
  void confirmarDevuelveNotFoundSiLaEntregaNoExiste() {
    when(ctx.pathParam("id")).thenReturn("1");
    when(rutasRepository.buscarEntregaPorId(1L)).thenReturn(Optional.empty());

    controller.confirmar(ctx);

    verify(ctx).status(HttpStatus.NOT_FOUND);
    verifyNoInteractions(notificadorEntregas);
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
  void reingresarADepositoDevuelveLaEntregaYNoAvisaADonaciones() {
    Entrega entrega = entregaNoRecibida();
    when(ctx.pathParam("id")).thenReturn("1");
    when(rutasRepository.buscarEntregaPorId(1L)).thenReturn(Optional.of(entrega));

    controller.reingresarADeposito(ctx);

    assertEquals(EstadoEntrega.PENDIENTE, entrega.getEstado());
    verify(ctx).json(entrega);
    verifyNoInteractions(notificadorEntregas);
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

  @Test
  void siFallaElAvisoADonacionesDevuelveConflict() {
    Entrega entrega = entregaEnTraslado();
    when(ctx.pathParam("id")).thenReturn("1");
    when(rutasRepository.buscarEntregaPorId(1L)).thenReturn(Optional.of(entrega));
    when(rutasRepository.buscarRutaPorEntregaId(1L)).thenReturn(Optional.of(rutaCon(entrega)));
    doThrow(new IllegalStateException("No se pudo notificar a donaciones-service"))
        .when(notificadorEntregas).avisarEntregada(eq(entrega), anyString());

    controller.confirmar(ctx);

    verify(ctx).status(HttpStatus.CONFLICT);
    verify(ctx, never()).json(entrega);
  }

  private Entrega entregaPendiente() {
    return new Entrega(List.of(), "Calle 123", "Comedor Sol");
  }

  private Entrega entregaEnTraslado() {
    Entrega entrega = entregaPendiente();
    entrega.iniciarTraslado();
    return entrega;
  }

  private Entrega entregaNoRecibida() {
    Entrega entrega = entregaEnTraslado();
    entrega.marcarNoRecibida();
    return entrega;
  }

  private Ruta rutaCon(Entrega entrega) {
    return new Ruta(new Camion("AB123CD", 10, 2, 1000), List.of(entrega));
  }
}
