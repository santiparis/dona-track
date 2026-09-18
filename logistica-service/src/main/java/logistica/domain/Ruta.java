package logistica.domain;

import javax.persistence.CascadeType;
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

  // cascade ALL: las entregas nacen y mueren con la ruta, no se guardan por su cuenta
  @OneToMany(cascade = CascadeType.ALL)
  @JoinColumn(name = "ruta_id")
  private List<Entrega> entregas;

  @Enumerated(EnumType.STRING)
  private EstadoRuta estado;


  public Ruta(Camion camion, List<Entrega> entregas) {
    this.camion = camion;
    // asignarle una ruta ocupa al camion: el planificador no se lo ofrece a otra
    camion.ocupar();
    this.entregas = entregas;
    this.estado = EstadoRuta.PLANIFICADA;
  }

  protected Ruta(){}

  public Long getId() {
    return id;
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

  // la ruta termino cuando ninguna entrega quedo en traslado: ahi se libera el camion
  public void completarSiTermino() {
    if (estado == EstadoRuta.EN_CURSO && this.entregasPendientes() == 0) {
      this.estado = EstadoRuta.COMPLETADA;
      this.camion.liberar();
    }
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