package logistica.domain;

import java.util.List;

public class Ruta {
  Camion camion;
  List<Entrega> entregas;

  public Ruta(Camion camion, List<Entrega> entregas) {
    this.camion = camion;
    this.entregas = entregas;
  }

  public Camion getCamion() {
    return camion;
  }

  public List<Entrega> getEntregas() {
    return entregas;
  }

  public long entregasPendientes() {
    return this.entregas.stream().filter(e -> e.getEstado() == EstadoEntrega.EN_TRASLADO).count();
  }
}
