package logistica.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;


public class Camion {
  private Long id;
  private String patente;
  private double volumen;
  private double altura;
  private double cargaMax;
  Coordenadas localizacion = null;
  double velocidad = 0;
  Boolean disponibilidad;

  // los jsonProperty los usara jackson para crear los objetos de json a dominio
  @JsonCreator
  public Camion(@JsonProperty("patente") String patente,
                @JsonProperty("volumen") double volumen,
                @JsonProperty("altura") double altura,
                @JsonProperty("cargaMax") double cargaMax) {
    this.patente = patente;
    this.volumen = volumen;
    this.altura = altura;
    this.cargaMax = cargaMax;
    this.disponibilidad = true;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
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

  public double getVelocidad() {
    return velocidad;
  }

  public void setVelocidad(double velocidad) {
    this.velocidad = velocidad;
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

  public void setVolumen(double nuevoVolumen) {
    this.volumen = nuevoVolumen;
  }

  public void actualizarDatos(String nuevaPatente, double nuevoVolumen,
                              double nuevaAltura, double nuevaCargaMax) {
    if(!this.getPatente().equals(nuevaPatente)) {
      this.patente = nuevaPatente;
    }
    this.volumen = nuevoVolumen;
    this.altura = nuevaAltura;
    this.cargaMax = nuevaCargaMax;
  }

  public void actualizarVelocidad(double nuevaVelocidad) {
    if(nuevaVelocidad < 0) {
      throw new IllegalArgumentException("La velocidad no puede ser negativa");
    }
    this.velocidad = nuevaVelocidad;
  }
}


