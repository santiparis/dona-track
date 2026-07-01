package logistica.domain;

public class Camion {
  final String patente;
  final float volumen;
  final float altura;
  final float cargaMax;
  Coordenadas localizacion = null;

  public Camion(String patente, float volumen, float altura, float cargaMax) {
    this.patente = patente;
    this.volumen = volumen;
    this.altura = altura;
    this.cargaMax = cargaMax;
  }

  public void actualizarLocalizacion(Coordenadas nuevaLocalizacion) {
    this.localizacion = nuevaLocalizacion;
  }
}
