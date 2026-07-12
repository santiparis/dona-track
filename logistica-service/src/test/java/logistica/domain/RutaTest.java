package logistica.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class RutaTest {

  private Entrega entrega1;
  private Entrega entrega2;
  private Ruta ruta;

  @BeforeEach
  void setUp() {
    Camion camion = new Camion("AB123CD", 10, 2, 1000);
    entrega1 = new Entrega(new ArrayList<>(), new Coordenadas(-34.6, -58.4));
    entrega2 = new Entrega(new ArrayList<>(), new Coordenadas(-34.5, -58.3));
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
  void noSePuedeIniciarUnaRutaQueYaEstaEnCurso() {
    ruta.iniciar();
    assertThrows(IllegalStateException.class, () -> ruta.iniciar());
  }
}