package donaciones.domain.algoritmos;

import donaciones.domain.Donacion;
import donaciones.domain.EntidadBeneficiaria;

import java.util.List;
import java.util.Comparator;

public class PrioridadSubAtendidos implements EstrategiaAsignacion {

  @Override
  public List<EntidadBeneficiaria> sugerirEntidades(Donacion donacion, List<EntidadBeneficiaria> entidades) {
    return entidades
            .stream()
            .sorted(Comparator.comparingInt(EntidadBeneficiaria::getDonacionesUltimoTrimestre))
            .limit(10)
            .toList();
  }
}
