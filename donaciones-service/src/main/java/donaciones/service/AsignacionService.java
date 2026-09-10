package donaciones.service;

import donaciones.domain.Donacion;
import donaciones.domain.EntidadBeneficiaria;
import donaciones.dto.DonacionLogisticaDTO;
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
  public List<EntidadRankingDTO> ejecutarAlgoritmoYObtenerRanking(Long donacionId, String criterio) {
    Optional<Donacion> donacionOpt = donacionRepository.buscarPorId(donacionId);

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
  public void confirmarAsignacion(Long donacionId, Long idEntidad, String nombreEntidad) {
    Optional<Donacion> donacionOpt = donacionRepository.buscarPorId(donacionId);
    Optional<EntidadBeneficiaria> entidadOpt = entidadRepository.obtenerPorId(idEntidad);

    if (donacionOpt.isPresent() && entidadOpt.isPresent()) {
      Donacion donacion = donacionOpt.get();
      EntidadBeneficiaria entidad = entidadOpt.get();
      donacion.asignarA(entidad);

      // TODO: estas notificaciones pasan al controller junto con la asignacion.
      donacion.getDonante().notificar(
          "Su donación ha sido asignada a la entidad: " + entidad.getRazonSocial());
      entidad.notificar("Se le ha asignado satisfactoriamente una nueva donación.");

      try {
        DonacionLogisticaDTO dto = new DonacionLogisticaDTO(
                donacionId,
                donacion.getBien().getCantidad(),
                "unidades",
                "direccion",
                nombreEntidad
        );
        logisticaAPICalls.enviarDonaciones(List.of(dto)).execute();
      } catch (Exception e) {
        System.out.println("Error al conectar con logistica");
      }
    } else {
      throw new IllegalArgumentException("No se encontro la donacion");
    }
  }
}