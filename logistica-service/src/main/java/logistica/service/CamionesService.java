package logistica.service;

import logistica.domain.Camion;
import logistica.domain.Coordenadas;
import logistica.dto.CamionDTO;
import logistica.dto.LocalizacionDTO;
import logistica.repository.CamionesRepository;

import java.util.List;
import java.util.NoSuchElementException;

public class CamionesService {
  private final CamionesRepository camionesRepository;

  public CamionesService(CamionesRepository camionesRepository) {
    this.camionesRepository = camionesRepository;
  }

  public List<Camion> getCamiones() {
    return this.camionesRepository.obtenerTodos();
  }

  public Camion getCamionPorId(Long id) {
    return this.camionesRepository.buscarPorId(id)
        .orElseThrow(() -> new NoSuchElementException("Camión inexistente"));
  }

  public void postCamion(CamionDTO dto) {
    this.camionesRepository.agregar(new Camion(dto.patente(), dto.volumen(), dto.altura(), dto.cargaMax()));
  }

  public Camion actualizarCamion(Long id, CamionDTO dto) {
    Camion camionActual = this.camionesRepository.buscarPorId(id)
        .orElseThrow(() -> new NoSuchElementException("Camión inexistente"));

    Camion camionActualizado = new Camion(dto.patente() != null ? dto.patente() : camionActual.getPatente(), dto.volumen(), dto.altura(), dto.cargaMax());
    camionActualizado.setId(id);
    camionActualizado.setLocalizacion(camionActual.getLocalizacion());
    camionActualizado.setDisponibilidad(camionActual.estaDisponible());

    this.camionesRepository.reemplazarCamionPorId(id, camionActualizado);

    return camionActualizado;
  }

  public void deleteCamion(Long id) {
    this.camionesRepository.buscarPorId(id)
        .orElseThrow(() -> new NoSuchElementException("Camión inexistente"));

    this.camionesRepository.eliminarPorId(id);
  }

  public void actualizarLocalizacion(Long id, LocalizacionDTO dto) {
    Camion camion = this.camionesRepository.buscarPorId(id)
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
