package donaciones.domain.algoritmos;

import donaciones.domain.Donacion;
import javax.persistence.*;
import donaciones.domain.EntidadBeneficiaria;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Entity
@Table(name = "sugerencia_asignacion")
public class SugerenciaAsignacion {

  @Id @Column(name = "donacion_id") private Long id;
  @OneToOne(optional = false) @JoinColumn(name = "donacion_id", insertable = false, updatable = false) private Donacion donacion;
  @ManyToMany private List<EntidadBeneficiaria> coincidentes;
  @OneToMany(mappedBy = "sugerencia", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<SugerenciaAlgoritmo> resultadosPorAlgoritmo = new ArrayList<>();

  protected SugerenciaAsignacion() { }

  public SugerenciaAsignacion(
      Long id,
      Donacion donacion,
      List<EntidadBeneficiaria> coincidentes,
      Map<String, List<EntidadBeneficiaria>> entidadesPorAlgoritmo
  ) {
    this.id = id;
    this.donacion = donacion;
    this.coincidentes = new ArrayList<>(coincidentes);
    entidadesPorAlgoritmo.forEach((algoritmo, entidades) ->
        resultadosPorAlgoritmo.add(new SugerenciaAlgoritmo(algoritmo, this, entidades)));
  }

  public Donacion getDonacion() {
    return this.donacion;
  }

  public Map<String, List<EntidadBeneficiaria>> getEntidadesPorAlgoritmo() {
    return resultadosPorAlgoritmo.stream().collect(Collectors.toMap(
        SugerenciaAlgoritmo::getAlgoritmo,
        SugerenciaAlgoritmo::getEntidades,
        (existente, duplicada) -> duplicada,
        LinkedHashMap::new
    ));
  }

  public List<EntidadBeneficiaria> getCoincidencias() {
    return this.coincidentes;
  }

  public List<EntidadBeneficiaria> coincidentes() {
    return this.coincidentes;
  }

  public Map<String, List<EntidadBeneficiaria>> entidadesPorAlgoritmo() {
    return getEntidadesPorAlgoritmo();
  }

  public Long donacionId() {
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
    return getEntidadesPorAlgoritmo().values().stream()
        .flatMap(List::stream)
        .anyMatch(entidad -> entidad.getId() != null && entidad.getId().equals(entidadId));
  }
}
