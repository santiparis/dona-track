import java.util.List;
import java.util.Comparator;
import java.util.stream.Collectors;

public class PrioridadSubAtendidos implements EstrategiaAsignacion {

  @Override
  public List<EntidadBeneficiaria> sugerirEntidades(DonacionIndependiente donacion, List<EntidadBeneficiaria> entidades) {
    return entidades.stream()
            .sorted(Comparator.comparingLong(EntidadBeneficiaria::getDonacionesRecibidasUltimoTrimestre))
            .limit(10).collect(Collectors.toList());
  }
}
