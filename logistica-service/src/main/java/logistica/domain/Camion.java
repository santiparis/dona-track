package logistica.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Camion {
  private Long id;
  final String patente;
  final double volumen;
  final double altura;
  final double cargaMax;
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
}


