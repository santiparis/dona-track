package logistica.domain;

import java.util.List;

public class Entrega {
  static Integer entregaID = 1;
  final String id;
  final List<Donacion> listaDonaciones;
  Coordenadas destino;
  EstadoEntrega estado;


  public Entrega(List<Donacion> listaDonaciones, Coordenadas destino) {
    this.listaDonaciones = listaDonaciones;
    this.id = entregaID.toString();
    entregaID++;

    this.destino = destino;
    this.estado = EstadoEntrega.PENDIENTE;
  }

  public String getId() {
    return id;
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

  public void agregarDonacion(Donacion donacion){
    this.listaDonaciones.add(donacion);
  }
}
