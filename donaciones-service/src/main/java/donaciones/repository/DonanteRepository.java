package donaciones.repository;

import donaciones.domain.donante.Persona;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DonanteRepository {
  private static final List<Persona> baseDeDatosSimulada = new ArrayList<>();
  private static long idSequence = 1L;

  public void guardar(Persona persona) {
    if (persona.getId() == null) {
      persona.setId(idSequence++);
    }
    if (!baseDeDatosSimulada.contains(persona)) {
      baseDeDatosSimulada.add(persona);
    }
  }

  public List<Persona> obtenerTodos() {
    return baseDeDatosSimulada;
  }

  public Optional<Persona> buscarPorId(Long id) {
    return baseDeDatosSimulada.stream().filter(persona -> persona.getId() != null && persona.getId().equals(id)).findFirst();
  }

  public void borrarPorId(Long id) {
    baseDeDatosSimulada.removeIf(persona -> persona.getId() != null && persona.getId().equals(id));
  }
}
