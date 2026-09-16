package donaciones.repository;

import donaciones.domain.algoritmos.SugerenciaAsignacion;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SugerenciaAsignacionRepository {

  private static final List<SugerenciaAsignacion> sugerencias = new ArrayList<>();

  public void guardar(SugerenciaAsignacion sugerencia) {
    sugerencias.add(sugerencia);
  }

  public List<SugerenciaAsignacion> obtenerTodas() {
    return new ArrayList<>(sugerencias);
  }

  public void limpiar() {
    sugerencias.clear();
  }

  public void eliminar(SugerenciaAsignacion sugerencia) {
    sugerencias.remove(sugerencia);
  }

  public Optional<SugerenciaAsignacion> buscarPorID(Long id) {
    return sugerencias.stream()
        .filter(sugerencia -> sugerencia.getID() != null && sugerencia.getID().equals(id))
        .findFirst();
  }
}
