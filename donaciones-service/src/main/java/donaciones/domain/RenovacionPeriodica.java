package donaciones.domain;

import java.time.LocalDate;

public class RenovacionPeriodica implements PoliticaDeRenovacion {
  LocalDate fechaInicio;
  LocalDate fechaFin;
  Periodo periodo;

  public RenovacionPeriodica(LocalDate fechaInicio, Periodo periodo) {
    this.fechaInicio = fechaInicio;
    this.periodo = periodo;
    this.fechaFin = this.calcularVencimiento();
  }

  @Override
  public boolean seRenueva() {
    return true;
  }

  @Override
  public boolean estaVencida() {
    return this.fechaFin.isBefore(LocalDate.now());
  }

  public void setFechaInicio(LocalDate fechaInicio) {
    this.fechaInicio = fechaInicio;
    this.fechaFin = calcularVencimiento();
  }

  private LocalDate calcularVencimiento() {
    if (this.periodo == Periodo.SEMANAL) {
      return this.fechaInicio.plusDays(7);
    }
    return this.fechaInicio.plusMonths(1);
  }
}
