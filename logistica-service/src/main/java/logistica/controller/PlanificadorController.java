package logistica.controller;

import io.javalin.http.Context;
import logistica.domain.Camion;
import logistica.domain.Donacion;
import logistica.domain.Entrega;
import logistica.domain.Ruta;
import logistica.planificacion.ClientePlanificador;
import logistica.repository.CamionesRepository;
import logistica.repository.DonacionesRepository;
import logistica.repository.RutasRepository;
import logistica.retrofit_client.PlanificacionCallbackRequest;
import logistica.retrofit_client.PlanificacionCallbackRequest.AsignacionCamion;
import logistica.retrofit_client.PlanificacionCallbackRequest.ParadaPlanificada;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PlanificadorController {
  private static final Logger logger = LoggerFactory.getLogger(PlanificadorController.class);

  private static final int TAMANIO_MAXIMO_BATCH = 100;

  private final CamionesRepository camionesRepository;
  private final RutasRepository rutasRepository;
  private final DonacionesRepository donacionesRepository;
  private final ClientePlanificador clientePlanificador;

  public PlanificadorController(CamionesRepository camionesRepository,
                                RutasRepository rutasRepository,
                                DonacionesRepository donacionesRepository,
                                ClientePlanificador clientePlanificador) {
    this.camionesRepository = camionesRepository;
    this.rutasRepository = rutasRepository;
    this.donacionesRepository = donacionesRepository;
    this.clientePlanificador = clientePlanificador;
  }

  public record MensajeResponse(String mensaje) { }

  // lo dispara el cron (o a demanda): manda un batch de pendientes a planificar
  public void planificar(Context ctx) {
    try {
      List<Donacion> pendientes = donacionesRepository.obtenerTodas();
      if (pendientes.isEmpty()) {
        ctx.status(200).json(new MensajeResponse("No hay donaciones pendientes de planificar"));
        return;
      }

      List<Donacion> batch = pendientes.stream().limit(TAMANIO_MAXIMO_BATCH).toList();
      List<Camion> disponibles = camionesRepository.obtenerDisponibles();

      // solo se sacan del pool si el planificador confirmo que las recibio;
      // las que sobran del batch quedan para la proxima corrida
      if (clientePlanificador.enviarAPlanificar(batch, disponibles)) {
        donacionesRepository.remover(batch);
      }

      ctx.status(202).json(new MensajeResponse("Planificacion disparada"));
    } catch (IllegalStateException e) {
      logger.warn("No se pudo contactar al planificador: {}", e.getMessage());
      ctx.status(502).json(new MensajeResponse("No se pudo contactar al planificador"));
    }
  }

  // callback: el planificador externo devuelve las rutas armadas
  public void obtenerRutas(Context ctx) {
    try {
      var body = ctx.bodyAsClass(PlanificacionCallbackRequest.class);

      // se arman todas antes de guardar ninguna: el callback es un plan completo,
      // guardarlo a medias dejaria donaciones fuera del pool y sin ruta
      List<Ruta> rutas = body.asignaciones().stream().map(this::armarRuta).toList();
      rutas.forEach(rutasRepository::agregar);
      donacionesRepository.agregarTodos(body.donacionesNoAsignadas());

      ctx.status(201).json(rutas);
    } catch (IllegalArgumentException e) {
      logger.warn("Callback de rutas invalido: {}", e.getMessage());
      ctx.status(400).json(new MensajeResponse(e.getMessage()));
    } catch (NoSuchElementException e) {
      logger.warn("Callback de rutas con referencia inexistente: {}", e.getMessage());
      ctx.status(404).json(new MensajeResponse(e.getMessage()));
    } catch (RuntimeException e) {
      logger.error("Error inesperado al procesar callback de rutas", e);
      ctx.status(500).json(new MensajeResponse("No se pudieron procesar las rutas"));
    }
  }

  private Ruta armarRuta(AsignacionCamion asignacion) {
    Camion camion = camionesRepository.buscarPorPatente(asignacion.patenteCamion())
        .orElseThrow(() -> new NoSuchElementException("Camion no encontrado: " + asignacion.patenteCamion()));

    List<Entrega> entregas = asignacion.paradas().stream()
        .map(PlanificadorController::armarEntrega)
        .toList();

    return new Ruta(camion, entregas);
  }

  // todas las donaciones de una parada van al mismo destino, por eso alcanza con la primera
  private static Entrega armarEntrega(ParadaPlanificada parada) {
    Donacion primera = parada.donaciones().get(0);
    return new Entrega(new ArrayList<>(parada.donaciones()), primera.getDestino(), primera.getEntidadNombre());
  }
}
