package logistica.service;

import logistica.retrofit_client.PlanificacionCallbackRequest;
import logistica.retrofit_client.PlanificacionCallbackRequest.AsignacionCamion;
import logistica.retrofit_client.PlanificacionCallbackRequest.ParadaPlanificada;
import logistica.retrofit_client.PlanificadorAPICalls;
import logistica.retrofit_client.PlanificadorAPICalls.PlanificacionRequest;
import logistica.domain.Camion;
import logistica.domain.Donacion;
import logistica.domain.Entrega;
import logistica.domain.Ruta;
import logistica.repository.CamionesRepository;
import logistica.repository.DonacionesRepository;
import logistica.repository.RutasRepository;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

public class PlanificadorService {

  private static final int TAMANIO_MAXIMO_BATCH = 100;

  private final CamionesRepository camionesRepository;
  private final RutasRepository rutasRepository;
  private final DonacionesRepository donacionesRepository;
  private final PlanificadorAPICalls planificadorAPICalls;

  public PlanificadorService(CamionesRepository camionesRepository,
                              RutasRepository rutasRepository,
                              DonacionesRepository donacionesRepository,
                              PlanificadorAPICalls planificadorAPICalls) {
    this.camionesRepository = camionesRepository;
    this.rutasRepository = rutasRepository;
    this.donacionesRepository = donacionesRepository;
    this.planificadorAPICalls = planificadorAPICalls;
  }

  public void enviarPlanificacion() throws IOException {
    List<Donacion> pendientes = donacionesRepository.obtenerTodas();
    if (pendientes.isEmpty()) {
      return;
    }

    List<Donacion> batch = pendientes.stream().limit(TAMANIO_MAXIMO_BATCH).toList();
    List<Camion> disponibles = camionesRepository.obtenerDisponibles();

    var request = new PlanificacionRequest(batch, disponibles);
    var response = planificadorAPICalls.enviarDonacionesAsignadas(request).execute();

    if (response.isSuccessful()) {
      donacionesRepository.remover(batch);
    }
  }

  public List<Ruta> procesarPlanificacion(PlanificacionCallbackRequest resultado) {
    List<Ruta> rutasCreadas = new ArrayList<>();

    for (AsignacionCamion asignacion : resultado.asignaciones()) {
      Camion camion = camionesRepository.buscarPorPatente(asignacion.patenteCamion())
          .orElseThrow(() -> new NoSuchElementException("Camion no encontrado: " + asignacion.patenteCamion()));

      List<Entrega> entregas = new ArrayList<>();
      for (ParadaPlanificada parada : asignacion.paradas()) {
        Donacion primeraDonacion = parada.donaciones().get(0);
        String destino = primeraDonacion.getDestino();
        String entidadNombre = primeraDonacion.getEntidadNombre();
        entregas.add(new Entrega(new ArrayList<>(parada.donaciones()), destino, entidadNombre));
      }

      Ruta ruta = new Ruta(camion, entregas);
      rutasRepository.agregar(ruta);
      rutasCreadas.add(ruta);
    }

    donacionesRepository.agregarTodos(resultado.donacionesNoAsignadas());

    return rutasCreadas;
  }
}