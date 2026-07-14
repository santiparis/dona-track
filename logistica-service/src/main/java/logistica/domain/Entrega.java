package logistica.domain;

import logistica.retrofit_client.DonacionesAPICalls;

import java.util.List;
import java.util.UUID;

public class Entrega {
  final String id;
  final List<Donacion> listaDonaciones;
  String destino;
  String entidadNombre;
  EstadoEntrega estado;


  public Entrega(List<Donacion> listaDonaciones, String destino, String entidadNombre) {
    this.listaDonaciones = listaDonaciones;
    this.id = UUID.randomUUID().toString();

    this.destino = destino;
    this.entidadNombre = entidadNombre;
    this.estado = EstadoEntrega.PENDIENTE;
  }

  public String getId() {
    return id;
  }

  public EstadoEntrega getEstado() {
    return estado;
  }

  public String getDestino() {
    return destino;
  }

  public String getEntidadNombre() {
    return entidadNombre;
  }

  public List<Donacion> getListaDonaciones() {
    return listaDonaciones;
  }

  public void iniciarTraslado() {
    if (estado != EstadoEntrega.PENDIENTE) {
      throw new IllegalStateException("No se puede iniciar traslado desde " + estado);
    }
    this.estado = EstadoEntrega.EN_TRASLADO;
  }

  public void marcarEntregada() {
    if (estado != EstadoEntrega.EN_TRASLADO) {
      throw new IllegalStateException("No se puede marcar entregada desde " + estado);
    }
    this.estado = EstadoEntrega.ENTREGADA;
  }

  public void marcarNoRecibida() {
    if (estado != EstadoEntrega.EN_TRASLADO) {
      throw new IllegalStateException("No se puede marcar no recibida desde " + estado);
    }
    this.estado = EstadoEntrega.NO_RECIBIDA;
  }

  public void reingresarADeposito() {
    if (estado != EstadoEntrega.NO_RECIBIDA) {
      throw new IllegalStateException("No se puede reingresar a depósito desde " + estado);
    }
    this.estado = EstadoEntrega.PENDIENTE;
  }

}
