package logistica.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.util.List;
@Entity
@Table(name = "entregas")
public class Entrega {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @OneToMany
  @JoinColumn(name = "entrega_id")
  private List<DonacionEncolada> listaDonaciones;


  private String destino;
  private String entidadNombre;

  @Enumerated(EnumType.STRING)
  private EstadoEntrega estado;

  @Column(unique = true)
  private String comprobante;


  public Entrega(List<DonacionEncolada> listaDonaciones, String destino, String entidadNombre) {
    this.listaDonaciones = listaDonaciones;
    this.destino = destino;
    this.entidadNombre = entidadNombre;
    this.estado = EstadoEntrega.PENDIENTE;
  }

  protected Entrega(){}

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

  public String getComprobante() {
    return comprobante;
  }

  public String getEntidadNombre() {
    return entidadNombre;
  }


  public List<DonacionEncolada> getListaDonaciones() {
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
    this.comprobante = "ENT-" + this.id;
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
