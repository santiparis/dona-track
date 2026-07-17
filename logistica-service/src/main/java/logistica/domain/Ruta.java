package logistica.domain;

import java.util.List;
public class Ruta {

  Long id;
  Camion camion;
  List<Entrega> entregas;
  EstadoRuta estado;

  public Ruta(Camion camion, List<Entrega> entregas) {
    this.camion = camion;
    this.entregas = entregas;
    this.estado = EstadoRuta.PLANIFICADA;
  }

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