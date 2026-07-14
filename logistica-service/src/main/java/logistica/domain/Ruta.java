package logistica.domain;

import java.util.List;
import java.util.UUID;

public class Ruta {

  final String id;
  Camion camion;
  List<Entrega> entregas;
  EstadoRuta estado;

  public Ruta(Camion camion, List<Entrega> entregas) {
    this.id = UUID.randomUUID().toString();
    this.camion = camion;
    this.entregas = entregas;
    this.estado = EstadoRuta.PLANIFICADA;
  }

  public String getId() {
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