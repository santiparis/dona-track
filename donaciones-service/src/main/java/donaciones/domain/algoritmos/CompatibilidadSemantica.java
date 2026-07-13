package donaciones.domain.algoritmos;

import donaciones.domain.DonacionIndependiente;
import donaciones.domain.EntidadBeneficiaria;

import java.util.List;

public class CompatibilidadSemantica implements EstrategiaAsignacion {

  @Override
  public List<EntidadBeneficiaria> sugerirEntidades(DonacionIndependiente donacion, List<EntidadBeneficiaria> entidades) {
    return entidades.stream().filter(entidad -> entidad.satisfaceNecesidad(donacion)).limit(10).toList();
  }
}