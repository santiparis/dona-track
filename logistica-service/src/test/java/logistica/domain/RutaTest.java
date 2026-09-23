package logistica.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class RutaTest {

  private Camion camion;
  private Entrega entrega1;
  private Entrega entrega2;
  private Ruta ruta;

  @BeforeEach
  void setUp() {
    camion = new Camion("AB123CD", 10, 2, 1000);
    entrega1 = new Entrega(new ArrayList<>(), "Calle 123", "Comedor Sol");
    entrega2 = new Entrega(new ArrayList<>(), "Calle 456", "Fundacion Esperanza");
    ruta = new Ruta(camion, List.of(entrega1, entrega2));
  }

  @Test
  void arrancaEnEstadoPlanificada() {
    assertEquals(EstadoRuta.PLANIFICADA, ruta.getEstado());
  }

  @Test
  void iniciarPasaAEnCurso() {
    ruta.iniciar();
    assertEquals(EstadoRuta.EN_CURSO, ruta.getEstado());
  }

  @Test
  void iniciarPasaTodasSusEntregasAEnTraslado() {
    ruta.iniciar();
    assertEquals(EstadoEntrega.EN_TRASLADO, entrega1.getEstado());
    assertEquals(EstadoEntrega.EN_TRASLADO, entrega2.getEstado());
  }

  @Test
  void asignarleUnaRutaOcupaAlCamion() {
    assertFalse(camion.estaDisponible());
  }

  @Test
  void laRutaSeCompletaYLiberaAlCamionCuandoNoQuedanEntregasEnTraslado() {
    ruta.iniciar();
    entrega1.marcarEntregada();
    entrega2.marcarNoRecibida();

    ruta.completarSiTermino();

    assertEquals(EstadoRuta.COMPLETADA, ruta.getEstado());
    assertTrue(camion.estaDisponible());
  }

  @Test
  void laRutaNoSeCompletaSiTodaviaQuedaUnaEntregaEnTraslado() {
    ruta.iniciar();
    entrega1.marcarEntregada();

    ruta.completarSiTermino();

    assertEquals(EstadoRuta.EN_CURSO, ruta.getEstado());
    assertFalse(camion.estaDisponible());
  }

  @Test
  void noSePuedeIniciarUnaRutaQueYaEstaEnCurso() {
    ruta.iniciar();
    assertThrows(IllegalStateException.class, () -> ruta.iniciar());
  }
}