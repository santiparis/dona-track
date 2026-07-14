package logistica.service;

import logistica.domain.Camion;
import logistica.dto.LocalizacionDTO;
import logistica.repository.CamionesRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class CamionesServiceTest {

  private CamionesRepository camionesRepository;
  private CamionesService camionesService;

  @BeforeEach
  public void setUp() {
    camionesRepository = new CamionesRepository();
    camionesRepository.agregar(new Camion("AB123CD", 10, 2, 1000));
    camionesService = new CamionesService(camionesRepository);
  }

  @Test
  public void actualizarLocalizacionGuardaCoordenadasYVelocidad() {
    camionesService.actualizarLocalizacion("AB123CD", new LocalizacionDTO(-34.60, -58.42, 47.5));

    Camion camion = camionesRepository.buscarPorPatente("AB123CD").orElseThrow();
    assertEquals(-34.60, camion.getLocalizacion().getLatitud());
    assertEquals(-58.42, camion.getLocalizacion().getLongitud());
    assertEquals(47.5, camion.getVelocidad());
  }

  @Test
  public void rechazaVelocidadNegativaYNoModificaElCamion() {
    var dto = new LocalizacionDTO(-34.60, -58.42, -5);

    assertThrows(IllegalArgumentException.class,
        () -> camionesService.actualizarLocalizacion("AB123CD", dto));

    Camion camion = camionesRepository.buscarPorPatente("AB123CD").orElseThrow();
    assertNull(camion.getLocalizacion());
    assertEquals(0, camion.getVelocidad());
  }

  @Test
  public void rechazaCoordenadaInvalida() {
    var dto = new LocalizacionDTO(120, -58.42, 20);

    assertThrows(IllegalArgumentException.class,
        () -> camionesService.actualizarLocalizacion("AB123CD", dto));
  }

  @Test
  public void fallaSiElCamionNoExiste() {
    var dto = new LocalizacionDTO(-34.60, -58.42, 20);

    assertThrows(NoSuchElementException.class,
        () -> camionesService.actualizarLocalizacion("NOEXISTE", dto));
  }
}