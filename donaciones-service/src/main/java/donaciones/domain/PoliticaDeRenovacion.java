package donaciones.domain;

import java.time.LocalDate;

public interface PoliticaDeRenovacion {
  boolean seRenueva();
  boolean estaVencida();

  /**
   * Datos de la representación single-table de Necesidad. Una política sin
   * renovación no ocupa columnas propias y, por lo tanto, devuelve null.
   */
  default LocalDate getFechaInicio() { return null; }
  default LocalDate getFechaFin() { return null; }
  default Periodo getPeriodo() { return null; }
}
