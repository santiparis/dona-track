package logistica.repository;

import logistica.domain.Camion;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CamionesRepository {
  private final List<Camion> camiones = new ArrayList<>();
  private static long idSequence = 1L;

  public void agregar(Camion camion) {
    if (camion.getId() == null) {
      camion.setId(idSequence++);
    }
    camiones.add(camion);
  }

  public void agregarTodos(List<Camion> nuevos) {
    for (Camion c : nuevos) {
      if (c.getId() == null) {
        c.setId(idSequence++);
      }
    }
    camiones.addAll(nuevos);
  }

  public Optional<Camion> buscarPorId(Long id) {
    return camiones.stream()
        .filter(c -> c.getId() != null && c.getId().equals(id))
        .findFirst();
  }

  public Optional<Camion> buscarPorPatente(String patente) {
    return camiones.stream()
        .filter(c -> c.getPatente().equals(patente))
        .findFirst();
  }

  public List<Camion> obtenerTodos() {
    return new ArrayList<>(camiones);
  }

  public List<Camion> obtenerDisponibles() {
    return camiones.stream()
        .filter(Camion::estaDisponible)
        .toList();
  }

  public void eliminarPorId(Long id) {
    camiones.removeIf(c -> c.getId() != null && c.getId().equals(id));
  }

  public void reemplazarCamionPorId(Long id, Camion nuevoCamion) {
    for (int i = 0; i < camiones.size(); i++) {
      Camion c = camiones.get(i);
      if (c.getId() != null && c.getId().equals(id)) {
        if (nuevoCamion.getId() == null) {
          nuevoCamion.setId(id);
        }
        camiones.set(i, nuevoCamion);
        return;
      }
    }
  }
}
