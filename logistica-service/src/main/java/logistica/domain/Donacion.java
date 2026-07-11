package logistica.domain;

public class Donacion {
  final String donacionID;
  final int cantidadBienes;
  final String unidad;


  public Donacion(String donacionID, int cantidad, String unidad) {
    this.donacionID = donacionID;
    this.cantidadBienes = cantidad;
    this.unidad = unidad;
  }
}
