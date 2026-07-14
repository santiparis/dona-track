package donaciones.repository;

import donaciones.domain.Donacion;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DonacionRepository {
  private static final List<Donacion> baseDeDatosSimulada = new ArrayList<>();
  private static int idSequence = 1;

  public void guardar(Donacion donacion) {
    baseDeDatosSimulada.add(donacion);
  }

  public List<Donacion> obtenerTodas() {
    return baseDeDatosSimulada;
  }

  public Optional<Donacion> buscarPorPosicion(int index) {
    if (index >= 0 && index < baseDeDatosSimulada.size()) {
      return Optional.of(baseDeDatosSimulada.get(index));
    }
    return Optional.empty();
  }

  public void borrarPorPosicion(int index) {
    if (index >= 0 && index < baseDeDatosSimulada.size()) {
      baseDeDatosSimulada.remove(index);
    }
  }
}
