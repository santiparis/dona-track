package logistica.domain;

import java.util.List;
import java.util.UUID;

public class Entrega {
  static Integer asignador_id = 1;
  final String id;
  final String donacionId;
  Coordenadas destino;
  EstadoEntrega estado;


  public Entrega(String donacionId, Coordenadas destino) {

    this.id = asignador_id.toString();
    asignador_id++;
    this.donacionId = donacionId;
    this.destino = destino;
    this.estado = EstadoEntrega.PENDIENTE;
  }

  public String getId() {
    return id;
  }

  public String getDonacionId() {
    return donacionId;
  }

  public EstadoEntrega getEstado() {
    return estado;
  }

  public Coordenadas getDestino() {
    return destino;
  }

  public void marcarEntregada() {
    this.estado = EstadoEntrega.ENTREGADA;
  }

  public void marcarNoRecibida() {
    this.estado = EstadoEntrega.NO_RECIBIDA;
  }

  public void actualizarEstado(EstadoEntrega estado) {
    this.estado = estado;
  }
}
