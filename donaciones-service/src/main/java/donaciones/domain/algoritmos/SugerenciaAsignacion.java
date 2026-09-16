package donaciones.domain.algoritmos;

import donaciones.domain.Donacion;
import donaciones.domain.EntidadBeneficiaria;

import java.util.List;
import java.util.Map;

public class SugerenciaAsignacion {

  private final Long id;
  private final Donacion donacion;
  private final List<EntidadBeneficiaria> coincidentes;
  private final Map<String, List<EntidadBeneficiaria>> entidadesPorAlgoritmo;

  public SugerenciaAsignacion(
      Long id,
      Donacion donacion,
      List<EntidadBeneficiaria> coincidentes,
      Map<String, List<EntidadBeneficiaria>> entidadesPorAlgoritmo
  ) {
    this.id = id;
    this.donacion = donacion;
    this.coincidentes = coincidentes;
    this.entidadesPorAlgoritmo = entidadesPorAlgoritmo;
  }

  public Donacion getDonacion() {
    return this.donacion;
  }

  public Map<String, List<EntidadBeneficiaria>> getEntidadesPorAlgoritmo() {
    return this.entidadesPorAlgoritmo;
  }

  public List<EntidadBeneficiaria> getCoincidencias() {
    return this.coincidentes;
  }

  public List<EntidadBeneficiaria> coincidentes() {
    return this.coincidentes;
  }

  public Map<String, List<EntidadBeneficiaria>> entidadesPorAlgoritmo() {
    return this.entidadesPorAlgoritmo;
  }

  public Long donacionId() {
    return this.id;
  }

  public Long getID() {
    return this.id;
  }

  public Long getId() {
    return this.id;
  }

  public boolean tieneCoincidencias() {
    return !coincidentes.isEmpty();
  }
}