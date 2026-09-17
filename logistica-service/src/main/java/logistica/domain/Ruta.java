package logistica.domain;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;

import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.util.List;

@Entity
@Table(name = "rutas")
public class Ruta {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne
  @JoinColumn(name = "camion_id")
  private Camion camion;

  @OneToMany
  @JoinColumn(name = "ruta_id")
  private List<Entrega> entregas;

  @Enumerated(EnumType.STRING)
  private EstadoRuta estado;


  public Ruta(Camion camion, List<Entrega> entregas) {
    this.camion = camion;
    this.entregas = entregas;
    this.estado = EstadoRuta.PLANIFICADA;
  }

  protected Ruta(){}

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Camion getCamion() {
    return camion;
  }

  public List<Entrega> getEntregas() {
    return entregas;
  }

  public EstadoRuta getEstado() {
    return estado;
  }

  public long entregasPendientes() {
    return this.entregas.stream().filter(e -> e.getEstado() == EstadoEntrega.EN_TRASLADO).count();
  }

  public void iniciar() {
    if (estado != EstadoRuta.PLANIFICADA) {
      throw new IllegalStateException("No se puede iniciar una ruta desde " + estado);
    }
    this.estado = EstadoRuta.EN_CURSO;
    for (Entrega entrega : entregas) {
      entrega.iniciarTraslado();
    }
  }
}