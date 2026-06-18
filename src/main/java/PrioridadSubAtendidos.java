import java.util.List;
import java.util.Comparator;
import java.util.stream.Collectors;

public class PrioridadSubAtendidos implements EstrategiaAsignacion {

  @Override
  public List<EntidadBeneficiaria> sugerirEntidades(DonacionIndependiente donacion, List<EntidadBeneficiaria> entidades) {
    // 1. Ordenar las entidades de menor a mayor cantidad de donaciones recibidas en los últimos 3 meses
    // 2. Limitar el resultado a 10.

    return entidades.stream()
        // .sorted(Comparator.comparingInt(EntidadBeneficiaria::getDonacionesUltimoTrimestre)) // (Ejemplo)
        .limit(10)
        .collect(Collectors.toList());
  }
}
