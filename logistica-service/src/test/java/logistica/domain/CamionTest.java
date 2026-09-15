package logistica.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class CamionTest {

  private Camion camion;

  @BeforeEach
  void setUp() {
    camion = new Camion("AB123CD", 10, 2, 1000);
  }

  @Test
  void actualizarLocalizacionGuardaCoordenadasYVelocidad() {
    camion.actualizarLocalizacion(new Coordenadas(-34.60, -58.42), 47.5);

    assertEquals(-34.60, camion.getLocalizacion().getLatitud());
    assertEquals(-58.42, camion.getLocalizacion().getLongitud());
    assertEquals(47.5, camion.getVelocidad());
  }

  @Test
  void rechazaVelocidadNegativaYNoModificaElCamion() {
    assertThrows(IllegalArgumentException.class,
        () -> camion.actualizarLocalizacion(new Coordenadas(-34.60, -58.42), -5));

    assertNull(camion.getLocalizacion());
    assertEquals(0, camion.getVelocidad());
  }

  @Test
  void rechazaLatitudInvalida() {
    assertThrows(IllegalArgumentException.class, () -> new Coordenadas(120, -58.42));
  }

  @Test
  void rechazaLongitudInvalida() {
    assertThrows(IllegalArgumentException.class, () -> new Coordenadas(-34.60, 200));
  }

  @Test
  void actualizarDatosReemplazaTodosLosDatosDelCamion() {
    camion.actualizarDatos("XY987ZW", 12, 2.5, 1500);

    assertEquals("XY987ZW", camion.getPatente());
    assertEquals(12, camion.getVolumen());
    assertEquals(2.5, camion.getAltura());
    assertEquals(1500, camion.getCargaMax());
  }

  @Test
  void unCamionNuevoArrancaDisponibleSinLocalizacionYDetenido() {
    assertEquals(true, camion.estaDisponible());
    assertNull(camion.getLocalizacion());
    assertEquals(0, camion.getVelocidad());
  }
}
