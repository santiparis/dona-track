package donaciones.domain.algoritmos;

import donaciones.domain.Donacion;
import donaciones.domain.EntidadBeneficiaria;

import java.util.*;

public class OrganizadorAsignaciones {

  List<EstrategiaAsignacion> estrategias;

  public OrganizadorAsignaciones (List<EstrategiaAsignacion> estrategias) {
    this.estrategias = estrategias;
  }

  public SugerenciaAsignacion procesarMatchmaking(Donacion donacion, List<EntidadBeneficiaria> todasLasEntidades) {

    Map<String, List<EntidadBeneficiaria>> entidadesPorAlgoritmo = new HashMap<>();

    this.estrategias.forEach(algoritmo -> entidadesPorAlgoritmo.put(algoritmo.toString(), algoritmo.sugerirEntidades(donacion, todasLasEntidades)));

    if (entidadesPorAlgoritmo.size() < 2) {
      return new SugerenciaAsignacion(donacion.getId(), donacion, Collections.emptyList(), entidadesPorAlgoritmo);
    }

    List<EntidadBeneficiaria> coincidentes = entidadesPorAlgoritmo
        .get(this.estrategias.get(0).toString())
        .stream()
        .filter(entidad -> this.apareceEnTodosAlgoritmos(entidad, entidadesPorAlgoritmo))
        .toList();

    if (coincidentes.isEmpty()) {
      return new SugerenciaAsignacion(donacion.getId(), donacion, Collections.emptyList(), entidadesPorAlgoritmo);
    }

    return new SugerenciaAsignacion(donacion.getId(), donacion, coincidentes, entidadesPorAlgoritmo);
  }

  public List<EntidadBeneficiaria> sugerirEntidadesPorCriterio(
      Donacion donacion,
      List<EntidadBeneficiaria> entidades,
      String criterio
  ) {
    EstrategiaAsignacion estrategia = estrategias.stream()
        .filter(candidata -> candidata.toString().toLowerCase().contains(criterio.toLowerCase()))
        .findFirst()
        .orElseThrow(() -> new IllegalArgumentException("No existe el criterio de asignación: " + criterio));

    return estrategia.sugerirEntidades(donacion, entidades);
  }

  private boolean apareceEnTodosAlgoritmos(EntidadBeneficiaria entidad, Map<String, List<EntidadBeneficiaria>> entidadesPorAlgortimo) {
    return entidadesPorAlgortimo
        .keySet()
        .stream()
        .allMatch(key -> entidadesPorAlgortimo.get(key).contains(entidad));
  }
}