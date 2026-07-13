package donaciones.domain.algoritmos;

import donaciones.domain.DonacionIndependiente;
import donaciones.domain.EntidadBeneficiaria;

import java.util.List;
import java.util.Comparator;
import java.util.stream.Collectors;

public class PrioridadSubAtendidos implements EstrategiaAsignacion {

  @Override
  public List<EntidadBeneficiaria> sugerirEntidades(DonacionIndependiente donacion, List<EntidadBeneficiaria> entidades) {
    return entidades
            .stream()
            .sorted(Comparator.comparingInt(EntidadBeneficiaria::getDonacionesUltimoTrimestre))
            .limit(10)
            .toList();
  }
}
