package donaciones.domain;

import java.time.LocalDate;

public interface PoliticaDeRenovacion {
  boolean seRenueva();
  boolean estaVencida();

  /** Datos persistidos por Necesidad para reconstruir esta política. */
  default LocalDate getFechaInicio() { return null; }
  default LocalDate getFechaFin() { return null; }
  default Periodo getPeriodo() { return null; }
}
