package logistica.repository;

import logistica.domain.Camion;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class RepositorioCamiones {
  private final List<Camion> camiones = new ArrayList<>();

  public void agregar(Camion camion) {
    camiones.add(camion);
  }

  public void agregarTodos(List<Camion> nuevos) {
    camiones.addAll(nuevos);
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
}