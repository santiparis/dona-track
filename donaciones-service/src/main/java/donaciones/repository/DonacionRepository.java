package donaciones.repository;

import donaciones.domain.Donacion;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DonacionRepository {
  private static final List<Donacion> donaciones = new ArrayList<>();
  private static long idSequence = 1L;

  public void guardar(Donacion donacion) {
    if (donacion.getId() == null) {
      donacion.setId(idSequence++);
    }
    if (!donaciones.contains(donacion)) {
      donaciones.add(donacion);
    }
  }

  public List<Donacion> obtenerTodas() {
    return donaciones;
  }

  public Optional<Donacion> buscarPorId(Long id) {
    return donaciones.stream()
            .filter(d -> d.getId() != null && d.getId().equals(id))
            .findFirst();
  }

  public void borrarPorId(Long id) {
    donaciones.removeIf(d -> d.getId() != null && d.getId().equals(id));
  }
}
