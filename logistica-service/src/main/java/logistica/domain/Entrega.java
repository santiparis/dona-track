package logistica.domain;

import java.util.ArrayList;
import java.util.List;


public class Entrega {
  Long id;
  final List<Donacion> listaDonaciones;
  String destino;
  String entidadNombre;
  EstadoEntrega estado;


  public Entrega(List<Donacion> listaDonaciones, String destino, String entidadNombre) {
    this.listaDonaciones = listaDonaciones;
    this.destino = destino;
    this.entidadNombre = entidadNombre;
    this.estado = EstadoEntrega.PENDIENTE;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
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

  public static Entrega desdeParada(List<Donacion> donaciones) {
    if (donaciones.isEmpty()) {
      throw new IllegalArgumentException("Una parada no puede tener cero donaciones");
    }
    String destino = donaciones.get(0).getDestino();
    String entidadNombre = donaciones.get(0).getEntidadNombre();
    return new Entrega(new ArrayList<>(donaciones), destino, entidadNombre);
  }
}
