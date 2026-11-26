package logistica.domain;

public enum EstadoCamion {
  DISPONIBLE("Disponible"),
  EN_RUTA("En ruta"),
  NO_DISPONIBLE("No disponible");

  // como se muestra el estado en la pantalla del admin
  private final String texto;

  EstadoCamion(String texto) {
    this.texto = texto;
  }

  public String texto() {
    return texto;
  }
}