package logistica.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class EntregaTest {

  private Entrega entrega;

  @BeforeEach
  void setUp() {
    entrega = new Entrega(new ArrayList<>(), "Calle 123", "Comedor Sol");
  }

  @Test
  void arrancaEnEstadoPendiente() {
    assertEquals(EstadoEntrega.PENDIENTE, entrega.getEstado());
  }

  @Test
  void iniciarTrasladoPasaDePendienteAEnTraslado() {
    entrega.iniciarTraslado();
    assertEquals(EstadoEntrega.EN_TRASLADO, entrega.getEstado());
  }

  @Test
  void noSePuedeIniciarTrasladoDeNuevoSiYaEstaEnTraslado() {
    entrega.iniciarTraslado();
    assertThrows(IllegalStateException.class, () -> entrega.iniciarTraslado());
  }

  @Test
  void marcarEntregadaPasaDeEnTrasladoAEntregada() {
    entrega.iniciarTraslado();
    entrega.marcarEntregada();
    assertEquals(EstadoEntrega.ENTREGADA, entrega.getEstado());
  }

  @Test
  void noSePuedeMarcarEntregadaSiTodaviaEstaPendiente() {
    assertThrows(IllegalStateException.class, () -> entrega.marcarEntregada());
  }

  @Test
  void marcarNoRecibidaPasaDeEnTrasladoANoRecibida() {
    entrega.iniciarTraslado();
    entrega.marcarNoRecibida();
    assertEquals(EstadoEntrega.NO_RECIBIDA, entrega.getEstado());
  }

  @Test
  void reingresarADepositoPasaDeNoRecibidaAPendiente() {
    entrega.iniciarTraslado();
    entrega.marcarNoRecibida();
    entrega.reingresarADeposito();
    assertEquals(EstadoEntrega.PENDIENTE, entrega.getEstado());
  }

  @Test
  void noSePuedeReingresarADepositoSiNoEstaNoRecibida() {
    assertThrows(IllegalStateException.class, () -> entrega.reingresarADeposito());
  }
}