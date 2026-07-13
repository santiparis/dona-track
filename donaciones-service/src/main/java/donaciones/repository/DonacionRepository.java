package donaciones.repository;

import donaciones.domain.DonacionIndependiente;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DonacionRepository {
  private static final List<DonacionIndependiente> baseDeDatosSimulada = new ArrayList<>();
  private static int idSequence = 1;

  public void guardar(DonacionIndependiente donacion) {
    baseDeDatosSimulada.add(donacion);
  }

  public List<DonacionIndependiente> obtenerTodas() {
    return baseDeDatosSimulada;
  }

  public Optional<DonacionIndependiente> buscarPorPosicion(int index) {
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
