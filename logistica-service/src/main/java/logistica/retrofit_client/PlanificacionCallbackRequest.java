package logistica.retrofit_client;

import logistica.domain.Donacion;

import java.util.List;

public record PlanificacionCallbackRequest(List<AsignacionCamion> asignaciones, List<Donacion> donacionesNoAsignadas) {

  public record AsignacionCamion(String patenteCamion, List<ParadaPlanificada> paradas) {}

  public record ParadaPlanificada(List<Donacion> donaciones) {}
}