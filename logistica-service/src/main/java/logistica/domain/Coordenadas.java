package logistica.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Coordenadas {
  private final double latitud;
  private final double longitud;

// los jsonProperty los usara jackson para crear los objetos de json a dominio
  @JsonCreator
  public Coordenadas(@JsonProperty("latitud") double latitud, @JsonProperty("longitud") double longitud) {
    if (latitud < -90 || latitud > 90) {
      throw new IllegalArgumentException("Latitud inválida");
    }

    if (longitud < -180 || longitud > 180) {
      throw new IllegalArgumentException("Longitud inválida");
    }

    this.latitud = latitud;
    this.longitud = longitud;
  }

  public double getLatitud() {
    return latitud;
  }

  public double getLongitud() {
    return longitud;
  }
}