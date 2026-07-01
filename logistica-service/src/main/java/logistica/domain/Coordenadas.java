package logistica.domain;

public class Coordenadas {
  private final double latitud;
  private final double longitud;

  public Coordenadas(double latitud, double longitud) {
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