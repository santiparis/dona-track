package donaciones.domain.algoritmos;

import donaciones.domain.EntidadBeneficiaria;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "sugerencia_algoritmo")
public class SugerenciaAlgoritmo {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "sugerencia_algoritmo_id")
  private Long id;

  @Column(name = "algoritmo", nullable = false)
  private String algoritmo;

  @ManyToOne(optional = false)
  @JoinColumn(name = "sugerencia_id", nullable = false)
  private SugerenciaAsignacion sugerencia;

  @ManyToMany
  @JoinTable(
      name = "sugerencia_algoritmo_entidad",
      joinColumns = @JoinColumn(name = "sugerencia_algoritmo_id"),
      inverseJoinColumns = @JoinColumn(name = "entidad_id")
  )
  private List<EntidadBeneficiaria> entidades = new ArrayList<>();

  protected SugerenciaAlgoritmo() { }

  SugerenciaAlgoritmo(String algoritmo, SugerenciaAsignacion sugerencia, List<EntidadBeneficiaria> entidades) {
    this.algoritmo = algoritmo;
    this.sugerencia = sugerencia;
    this.entidades = new ArrayList<>(entidades);
  }

  String getAlgoritmo() {
    return algoritmo;
  }

  List<EntidadBeneficiaria> getEntidades() {
    return entidades;
  }

  void setSugerencia(SugerenciaAsignacion sugerencia) {
    this.sugerencia = sugerencia;
  }
}
