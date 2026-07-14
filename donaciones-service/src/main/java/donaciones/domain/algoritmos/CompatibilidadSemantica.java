package donaciones.domain.algoritmos;

import donaciones.domain.Donacion;
import donaciones.domain.EntidadBeneficiaria;

import java.util.List;

public class CompatibilidadSemantica implements EstrategiaAsignacion {

  @Override
  public List<EntidadBeneficiaria> sugerirEntidades(Donacion donacion, List<EntidadBeneficiaria> entidades) {
    return entidades.stream().filter(entidad -> entidad.satisfaceNecesidad(donacion)).limit(10).toList();
  }
}