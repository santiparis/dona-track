package donaciones.controller;

import donaciones.domain.Donacion;
import donaciones.domain.PersonaAdministradora;

import java.time.LocalDate;
import java.util.List;

public class Notificador {

  public void donacionAsignada(Donacion donacion) {
    donacion.getDonante().notificar("Su donación ha sido asignada a la entidad: "
        + donacion.getEntidadBeneficiaria().getRazonSocial());
    donacion.getEntidadBeneficiaria().notificar("Se le ha asignado satisfactoriamente una nueva donación.");
  }

  public void inicioDeTraslado(Donacion donacion, String urlMapa) {
    String mensaje = "Su entrega está en camino. Siga el recorrido en tiempo real aquí: " + urlMapa;
    this.avisarADonanteYEntidad(donacion, mensaje);
  }

  public void rutaIniciada(List<Donacion> donaciones, String urlMapa) {
    donaciones.forEach(donacion -> this.inicioDeTraslado(donacion, urlMapa));
  }

  public void entregaConfirmada(Donacion donacion, String nombreCamion) {
    String mensaje = "Entrega realizada. Fecha/Hora: " + LocalDate.now()
        + " | Camión: " + nombreCamion;
    this.avisarADonanteYEntidad(donacion, mensaje);
  }

  public void entregaFallida(Donacion donacion, String motivo, List<PersonaAdministradora> administradoras) {
    String mensaje = "Alerta: Entrega no satisfactoria. Motivo: " + motivo;
    this.avisarADonanteYEntidad(donacion, mensaje);
    administradoras.forEach(administradora -> administradora.notificar(mensaje));
  }

  private void avisarADonanteYEntidad(Donacion donacion, String mensaje) {
    donacion.getDonante().notificar(mensaje);
    donacion.getEntidadBeneficiaria().notificar(mensaje);
  }
}
