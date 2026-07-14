package donaciones.service;

import donaciones.domain.DonacionIndependiente;
import donaciones.domain.EstadoDonacionIndependiente;
import donaciones.domain.EntidadBeneficiaria;
import donaciones.domain.eventos.DonacionAsignadaEvent;
import donaciones.dto.EntidadRankingDTO;
import donaciones.repository.DonacionRepository;
import donaciones.repository.EntidadBeneficiariaRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import donaciones.domain.algoritmos.EstrategiaAsignacion;
import donaciones.domain.algoritmos.PrioridadSubAtendidos;
import donaciones.domain.algoritmos.CompatibilidadSemantica;

public class AsignacionService {

  private final DonacionRepository donacionRepository;
  private final EntidadBeneficiariaRepository entidadRepository;
  public AsignacionService(DonacionRepository donacionRepository, EntidadBeneficiariaRepository entidadRepository) {
    this.donacionRepository = donacionRepository;
    this.entidadRepository = entidadRepository;
  }

  // ejecucion y ranking
  public List<EntidadRankingDTO> ejecutarAlgoritmoYObtenerRanking(int donacionId, String criterio) {
    Optional<DonacionIndependiente> donacionOpt = donacionRepository.buscarPorPosicion(donacionId);

    if (donacionOpt.isEmpty()) {
      throw new IllegalArgumentException("No existe la donacion");
    }

    DonacionIndependiente donacion = donacionOpt.get();
    List<EntidadBeneficiaria> todasLasEntidades = entidadRepository.obtenerTodas();
    EstrategiaAsignacion algoritmo;
    if (criterio.equalsIgnoreCase("prioridad")) {
      algoritmo = new PrioridadSubAtendidos();
    } else {
      algoritmo = new CompatibilidadSemantica();
    }

    List<EntidadBeneficiaria> entidadesGanadoras = algoritmo.sugerirEntidades(donacion, todasLasEntidades);

    return entidadesGanadoras.stream()
            .map(entidad -> new EntidadRankingDTO(
                    entidad.getRazonSocial()
            ))
            .toList();
  }

  // seleccion final
  public void confirmarAsignacion(int donacionId, Long idEntidad, String nombreEntidad) {
    Optional<DonacionIndependiente> donacionOpt = donacionRepository.buscarPorPosicion(donacionId);
    Optional<EntidadBeneficiaria> entidadOpt = entidadRepository.obtenerPorId(idEntidad);

    if (donacionOpt.isPresent() && entidadOpt.isPresent()) {
      DonacionIndependiente donacion = donacionOpt.get();
      donacion.setEstado(EstadoDonacionIndependiente.ASIGNADA);
      EntidadBeneficiaria entidad = entidadOpt.get();
      donacion.setEntidadBeneficiaria(entidad);

      // Enviar notificacion
      DonacionAsignadaEvent evento = new DonacionAsignadaEvent(donacion.getDonante(), entidad);
      evento.notificarAInvolucrados();

    } else {
      throw new IllegalArgumentException("No se encontro la donacion");
    }
  }
}