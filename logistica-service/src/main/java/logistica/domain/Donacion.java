package logistica.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Donacion {
  final Long donacionID;
  final int cantidadBienes;
  final String unidad;
  final String destino;
  final String entidadNombre;

  @JsonCreator
  public Donacion(@JsonProperty("donacionID") Long donacionID,
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
