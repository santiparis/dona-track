package donaciones.repository;

import donaciones.domain.donante.Persona;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Objects;

public class DonanteRepository {
  private static final List<Persona> baseDeDatosSimulada = new ArrayList<>();

  public void guardar(Persona persona) {
    baseDeDatosSimulada.add(persona);
  }

  public List<Persona> obtenerTodos() {
    return baseDeDatosSimulada;
  }

  public Optional<Persona> buscarPorDocumento(String documento) {
    return baseDeDatosSimulada.stream().filter(persona -> Objects.equals(persona.getDocumento(), documento)).findFirst();
  }

  public void borrarPorDocumento(String documento) {
    baseDeDatosSimulada.removeIf(persona -> persona.getDocumento().equals(documento));
  }
}
