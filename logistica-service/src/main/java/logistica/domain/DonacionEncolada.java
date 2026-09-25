package logistica.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "donaciones_encoladas")
public class DonacionEncolada {

  @Id
  private Long donacionID;
  private int cantidadBienes;
  private String unidad;
  private String destino;
  private String entidadNombre;

  @JsonCreator
  public DonacionEncolada(@JsonProperty("donacionID") Long donacionID,
                          @JsonProperty("cantidadBienes") int cantidad,
                          @JsonProperty("unidad") String unidad,
                          @JsonProperty("destino") String coordenadas,
                          @JsonProperty("entidadNombre") String entidadNombre) {
    this.donacionID = donacionID;
    this.cantidadBienes = cantidad;
    this.unidad = unidad;
    this.destino = coordenadas;
    this.entidadNombre = entidadNombre;
  }

  protected DonacionEncolada(){

  }
  public Long getDonacionID() {
    return donacionID;
  }

  public int getCantidadBienes() {
    return cantidadBienes;
  }

  public String getUnidad() {
    return unidad;
  }

  public String getDestino() {
    return destino;
  }

  public String getEntidadNombre() {
    return entidadNombre;
  }
}
