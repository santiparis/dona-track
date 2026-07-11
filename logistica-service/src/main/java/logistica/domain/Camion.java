package logistica.domain;

public class Camion {
  final String patente;
  final double volumen;
  final double altura;
  final double cargaMax;
  Coordenadas localizacion = null;
  Boolean disponibilidad;

  public Camion(String patente, double volumen, double altura, double cargaMax) {
    this.patente = patente;
    this.volumen = volumen;
    this.altura = altura;
    this.cargaMax = cargaMax;
    this.disponibilidad = true;
  }

  public void actualizarLocalizacion(Coordenadas nuevaLocalizacion) {
    this.localizacion = nuevaLocalizacion;
  }



  public Boolean estaDisponible(){
    return this.disponibilidad == true;
  }

  public Coordenadas getLocalizacion() {
    return localizacion;
  }

  public void setLocalizacion(Coordenadas localizacion) {
    this.localizacion = localizacion;
  }

  public double getCargaMax() {
    return cargaMax;
  }

  public double getAltura() {
    return altura;
  }

  public double getVolumen() {
    return volumen;
  }

  public String getPatente(){
    return this.patente;
  }

  public void setDisponibilidad(Boolean estado){
    this.disponibilidad = estado;
  }
}


