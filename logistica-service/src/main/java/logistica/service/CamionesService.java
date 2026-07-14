package logistica.service;

import logistica.domain.Camion;
import logistica.domain.Coordenadas;
import logistica.dto.CamionDTO;
import logistica.dto.LocalizacionDTO;
import logistica.repository.CamionesRepository;

import java.util.List;
import java.util.Objects;
import java.util.NoSuchElementException;

public class CamionesService {
  private final CamionesRepository camionesRepository;

  public CamionesService(CamionesRepository camionesRepository) {
    this.camionesRepository = camionesRepository;
  }

  public List<Camion> getCamiones() {
    return this.camionesRepository.obtenerTodos();
  }

  public void postCamion(CamionDTO dto) {
    this.camionesRepository.agregar(new Camion(dto.patente(), dto.volumen(), dto.altura(), dto.cargaMax()));
  }

  public Camion actualizarCamion(String patente, CamionDTO dto) {
    if (dto.patente() == null || !Objects.equals(patente, dto.patente())) {
      throw new IllegalArgumentException("La patente del camión debe coincidir con la de la ruta");
    }

    Camion camionActual = this.camionesRepository.buscarPorPatente(patente)
        .orElseThrow(() -> new NoSuchElementException("Camión inexistente"));

    Camion camionActualizado = new Camion(dto.patente(), dto.volumen(), dto.altura(), dto.cargaMax());
    camionActualizado.setLocalizacion(camionActual.getLocalizacion());
    camionActualizado.setDisponibilidad(camionActual.estaDisponible());

    this.camionesRepository.reemplazarCamion(patente, camionActualizado);

    return camionActualizado;
  }

  public void deleteCamion(String patente) {
    this.camionesRepository.buscarPorPatente(patente)
        .orElseThrow(() -> new NoSuchElementException("Camión inexistente"));

    this.camionesRepository.eliminarCamion(patente);
  }

  public void actualizarLocalizacion(String patente, LocalizacionDTO dto) {
    Camion camion = this.camionesRepository.buscarPorPatente(patente)
        .orElseThrow(() -> new NoSuchElementException("Camión inexistente"));

    if (dto.velocidad() < 0) {
      throw new IllegalArgumentException("La velocidad no puede ser negativa");
    }

    // la validación de lat/long la hace el constructor de Coordenadas
    Coordenadas localizacion = new Coordenadas(dto.latitud(), dto.longitud());
    camion.actualizarLocalizacion(localizacion);
    camion.setVelocidad(dto.velocidad());
  }
}
