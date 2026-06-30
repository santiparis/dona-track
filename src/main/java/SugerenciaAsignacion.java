import java.util.List;

public record SugerenciaAsignacion(
        List<EntidadBeneficiaria> coincidentes,
        List<EntidadBeneficiaria> sugerenciasSemanticas,
        List<EntidadBeneficiaria> sugerenciasSubAtendidas
) {

  public boolean tieneCoincidencias() {
    return !coincidentes.isEmpty();
  }
}