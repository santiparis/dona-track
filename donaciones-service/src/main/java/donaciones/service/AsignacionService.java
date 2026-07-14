package donaciones.service;

import donaciones.domain.Donacion;
import donaciones.domain.EstadoDonacionIndependiente;
import donaciones.domain.EntidadBeneficiaria;
import donaciones.domain.eventos.DonacionAsignadaEvent;
import donaciones.domain.eventos.PublicadorDeEventos;
import donaciones.dto.DonacionLogisticaDTO;
import donaciones.dto.EntidadRankingDTO;
import donaciones.repository.DonacionRepository;
import donaciones.repository.EntidadBeneficiariaRepository;
import donaciones.retrofit_client.LogisticaAPICalls;
import donaciones.retrofit_client.RetrofitConfig;
import java.util.List;
import java.util.Optional;
import donaciones.domain.algoritmos.EstrategiaAsignacion;
import donaciones.domain.algoritmos.PrioridadSubAtendidos;
import donaciones.domain.algoritmos.CompatibilidadSemantica;

public class AsignacionService {

  private final DonacionRepository donacionRepository;
  private final EntidadBeneficiariaRepository entidadRepository;
  private final PublicadorDeEventos publicador;
  private final LogisticaAPICalls logisticaAPICalls;
  
  public AsignacionService(
      DonacionRepository donacionRepository,
      EntidadBeneficiariaRepository entidadRepository,
      LogisticaAPICalls logisticaAPICalls,
      PublicadorDeEventos publicador
  ) {
    this.donacionRepository = donacionRepository;
    this.entidadRepository = entidadRepository;
    this.logisticaAPICalls = logisticaAPICalls;
    this.publicador = publicador;
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
  public void confirmarAsignacion(int donacionId, Long idEntidad, String nombreEntidad) {
    Optional<Donacion> donacionOpt = donacionRepository.buscarPorPosicion(donacionId);
    Optional<EntidadBeneficiaria> entidadOpt = entidadRepository.obtenerPorId(idEntidad);

    if (donacionOpt.isPresent() && entidadOpt.isPresent()) {
      Donacion donacion = donacionOpt.get();
      donacion.setEstado(EstadoDonacionIndependiente.ASIGNADA);
      EntidadBeneficiaria entidad = entidadOpt.get();
      donacion.setEntidadBeneficiaria(entidad);

      // Enviar notificacion
      publicador.publicar(new DonacionAsignadaEvent(donacion));
      
      //donacion.setEstado(EstadoDonacionIndependiente.ENTREGADA);
      try {
        DonacionLogisticaDTO dto = new DonacionLogisticaDTO(
                String.valueOf(donacionId),
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