package logistica.retrofit_client;

import logistica.domain.DonacionEncolada;

import java.util.List;

public record PlanificacionCallbackRequest(List<AsignacionCamion> asignaciones, List<DonacionEncolada> donacionesNoAsignadas) {

  public record AsignacionCamion(String patenteCamion, List<ParadaPlanificada> paradas) {}

  public record ParadaPlanificada(List<DonacionEncolada> donaciones) {}
}