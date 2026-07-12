package logistica.client;

import logistica.domain.Donacion;

import java.util.List;

public record PlanificacionCallbackRequest(List<AsignacionCamion> asignaciones, List<Donacion> donacionesNoAsignadas) {

  public record AsignacionCamion(String patenteCamion, List<ParadaPlanificada> paradas) {}

  public record ParadaPlanificada(double latitud, double longitud, List<Donacion> donaciones) {}
}