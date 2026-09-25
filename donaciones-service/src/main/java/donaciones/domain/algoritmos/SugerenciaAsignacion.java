package donaciones.domain.algoritmos;

import donaciones.domain.Donacion;
import javax.persistence.*;
import donaciones.domain.EntidadBeneficiaria;

import java.util.List;
import java.util.Map;


@Entity
@Table(name = "sugerencia_asignacion")
public class SugerenciaAsignacion {

  @Id @Column(name = "donacion_id") private Long id;
  @OneToOne(optional = false) @JoinColumn(name = "donacion_id", insertable = false, updatable = false) private Donacion donacion;
  @ManyToMany private List<EntidadBeneficiaria> coincidentes;
  @Transient private Map<String, List<EntidadBeneficiaria>> entidadesPorAlgoritmo;

  protected SugerenciaAsignacion() { }

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

  /** Regla de negocio: solo puede confirmarse una entidad propuesta por algún algoritmo. */
  public boolean incluyeEntidad(Long entidadId) {
    return entidadesPorAlgoritmo.values().stream()
        .flatMap(List::stream)
        .anyMatch(entidad -> entidad.getId() != null && entidad.getId().equals(entidadId));
  }
}
