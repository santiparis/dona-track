package donaciones.service;

import donaciones.domain.Donacion;
import donaciones.domain.EstadoDonacionIndependiente;
import donaciones.domain.EntidadBeneficiaria;
import donaciones.dto.EntidadRankingDTO;
import donaciones.repository.DonacionRepository;
import donaciones.repository.EntidadBeneficiariaRepository;
import donaciones.retrofit_client.LogisticaAPICalls;

import java.util.List;
import java.util.Optional;
import donaciones.domain.algoritmos.EstrategiaAsignacion;
import donaciones.domain.algoritmos.PrioridadSubAtendidos;
import donaciones.domain.algoritmos.CompatibilidadSemantica;

public class AsignacionService {

  private final DonacionRepository donacionRepository;
  private final EntidadBeneficiariaRepository entidadRepository;
  private final LogisticaAPICalls logisticaAPICalls;

  public AsignacionService(
      DonacionRepository donacionRepository,
      EntidadBeneficiariaRepository entidadRepository,
      LogisticaAPICalls logisticaAPICalls
  ) {
    this.donacionRepository = donacionRepository;
    this.entidadRepository = entidadRepository;
    this.logisticaAPICalls = logisticaAPICalls;
  }

  // ejecucion y ranking
  public List<EntidadRankingDTO> ejecutarAlgoritmoYObtenerRanking(int donacionId, String criterio) {
    Optional<Donacion> donacionOpt = donacionRepository.buscarPorPosicion(donacionId);

    if (donacionOpt.isEmpty()) {
      throw new IllegalArgumentException("No existe la donacion");
    }

    Donacion donacion = donacionOpt.get();
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
  public void confirmarAsignacion(int donacionId, String nombreEntidad) {
    Optional<Donacion> donacionOpt = donacionRepository.buscarPorPosicion(donacionId);
    if (donacionOpt.isPresent()) {
      Donacion donacion = donacionOpt.get();
      donacion.setEstado(EstadoDonacionIndependiente.ENTREGADA);
    } else {
      throw new IllegalArgumentException("No se encontro la donacion");
    }
  }
}