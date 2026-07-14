package donaciones.domain.algoritmos;

import donaciones.domain.DonacionIndependiente;
import donaciones.domain.EntidadBeneficiaria;

import java.util.List;

public class OrganizadorAsignaciones {

  private final EstrategiaAsignacion compatibilidadSemantica = new CompatibilidadSemantica();
  private final EstrategiaAsignacion prioridadSubAtendidos = new PrioridadSubAtendidos();

  public SugerenciaAsignacion procesarMatchmaking(DonacionIndependiente donacion, List<EntidadBeneficiaria> todasLasEntidades) {

    List<EntidadBeneficiaria> semanticas = compatibilidadSemantica.sugerirEntidades(donacion, todasLasEntidades);
    List<EntidadBeneficiaria> subAtendidas = prioridadSubAtendidos.sugerirEntidades(donacion, todasLasEntidades);

    List<EntidadBeneficiaria> coincidentes = semanticas.stream()
            .filter(subAtendidas::contains)
            .toList();

    return new SugerenciaAsignacion(coincidentes, semanticas, subAtendidas);
  }
}