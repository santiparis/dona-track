import java.util.List;

public class Entrega {
  Coordenadas destino;
  EstadoEntrega estado;
  List<DonacionIndependiente> donaciones;

  public Entrega(Coordenadas destino, List<DonacionIndependiente> donaciones) {
    this.destino = destino;
    this.estado = EstadoEntrega.EN_RUTA;
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
}
