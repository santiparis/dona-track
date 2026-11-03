package donaciones.controller;

import donaciones.domain.Donacion;
import donaciones.domain.EstadoDonacion;
import donaciones.domain.EntidadBeneficiaria;
import donaciones.domain.algoritmos.OrganizadorAsignaciones;
import donaciones.domain.algoritmos.SugerenciaAsignacion;
import donaciones.dto.AsignacionRequestDTO;
import donaciones.dto.EntidadRankingDTO;
import donaciones.grpc.LogisticaGrpcClient;
import donaciones.repository.DonacionRepository;
import donaciones.repository.EntidadBeneficiariaRepository;
import donaciones.repository.SugerenciaAsignacionRepository;
import donaciones.web.DatosMock;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import io.javalin.http.NotFoundResponse;
import logistica.grpc.TrazarRutaResponse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AsignacionesController {

  private final DonacionRepository donacionRepository;
  private final EntidadBeneficiariaRepository entidadRepository;
  private final SugerenciaAsignacionRepository sugerenciaRepository;
  private final OrganizadorAsignaciones organizadorAsignaciones;
  private final LogisticaGrpcClient logisticaGrpcClient;
  private final Notificador notificador;

  public AsignacionesController(
      DonacionRepository donacionRepository,
      EntidadBeneficiariaRepository entidadRepository,
      SugerenciaAsignacionRepository sugerenciaRepository,
      LogisticaGrpcClient logisticaGrpcClient,
      Notificador notificador,
      OrganizadorAsignaciones organizadorAsignaciones
  ) {
    this.donacionRepository = donacionRepository;
    this.entidadRepository = entidadRepository;
    this.sugerenciaRepository = sugerenciaRepository;
    this.logisticaGrpcClient = logisticaGrpcClient;
    this.notificador = notificador;
    this.organizadorAsignaciones = organizadorAsignaciones;
  }

  public List<SugerenciaAsignacion> procesarDonacionesEnDeposito() {
    List<Donacion> donacionesEnDeposito = donacionRepository.obtenerTodas().stream()
        .filter(donacion -> donacion.getEstado() == EstadoDonacion.EN_DEPOSITO)
        .toList();

    List<EntidadBeneficiaria> entidadesConNecesidadesActivas = entidadRepository.obtenerTodas().stream()
        .filter(entidad -> entidad.getNecesidades() != null && !entidad.getNecesidades().isEmpty())
        .toList();

    List<SugerenciaAsignacion> sugerenciasGeneradas = new ArrayList<>();

    for (Donacion donacion : donacionesEnDeposito) {
      SugerenciaAsignacion sugerencia = organizadorAsignaciones.procesarMatchmaking(
          donacion,
          entidadesConNecesidadesActivas
      );

      sugerenciaRepository.guardar(sugerencia);
      sugerenciasGeneradas.add(sugerencia);
    }

    return sugerenciasGeneradas;
  }

  public List<SugerenciaAsignacion> obtenerSugerenciasGuardadas() {
    return sugerenciaRepository.obtenerTodas();
  }

  // Ejecucion a demanda de los algoritmos; hace lo mismo que el cron ProcesarAsignaciones
  public void ejecutarAlgoritmos(Context ctx) {
    this.limpiarSugerencias();
    ctx.status(HttpStatus.CREATED).json(this.procesarDonacionesEnDeposito());
  }

  public void getSugerencias(Context ctx) {
    ctx.json(sugerenciaRepository.obtenerTodas());
  }

  public void getCoincidencias(Context ctx) {
    Long idDonacion = Long.parseLong(ctx.pathParam("id"));

    SugerenciaAsignacion sugerencia = obtenerSugerenciaPorDonacion(idDonacion);
    ctx.json(sugerencia.getCoincidencias());
  }

  public void getEntidadesPorAlgoritmo(Context ctx) {
    Long idDonacion = Long.parseLong(ctx.pathParam("id"));

    SugerenciaAsignacion sugerencia = obtenerSugerenciaPorDonacion(idDonacion);
    ctx.json(sugerencia.getEntidadesPorAlgoritmo());
  }

  public void asignarDonacion(Context ctx) {
    Long idSugerencia = Long.parseLong(ctx.pathParam("id"));
    AsignacionRequestDTO dto = ctx.bodyAsClass(AsignacionRequestDTO.class);

    if (dto == null || dto.idEntidad() == null) {
      throw new IllegalArgumentException("Se requiere idEntidad en el body de la request");
    }

    asignar(idSugerencia, dto.idEntidad(), dto.nombreEntidadSeleccionada());
  }

  // Compartido entre PATCH /api/sugerencias/{id}/asignaciones y el form de /admin/asignaciones/{id}
  private void asignar(Long idSugerencia, Long idEntidad, String nombreEntidad) {
    SugerenciaAsignacion sugerencia = this.sugerenciaRepository.buscarPorID(idSugerencia).orElseThrow();

    if (!sugerencia.incluyeEntidad(idEntidad)) {
      throw new IllegalArgumentException("La entidad seleccionada no aparece en ninguna de las listas generadas por los algoritmos");
    }

    EntidadBeneficiaria entidad = this.entidadRepository.buscarPorId(idEntidad)
        .orElseThrow(() -> new IllegalArgumentException("No existe la entidad especificada"));

    confirmarAsignacion(sugerencia.getDonacion(), entidad, nombreEntidad);

    this.sugerenciaRepository.eliminar(sugerencia);
  }

  /** Orquestación de caso de uso: repositorios, integración externa y respuesta HTTP pertenecen al controller MVC. */
  public List<EntidadRankingDTO> ejecutarAlgoritmoYObtenerRanking(Long donacionId, String criterio) {
    Donacion donacion = donacionRepository.buscarPorId(donacionId)
        .orElseThrow(() -> new IllegalArgumentException("No existe la donación"));
    return organizadorAsignaciones.sugerirEntidadesPorCriterio(
            donacion,
            entidadRepository.obtenerTodas(),
            criterio
        ).stream()
        .map(entidad -> new EntidadRankingDTO(entidad.getRazonSocial()))
        .toList();
  }

  private void confirmarAsignacion(Donacion donacion, EntidadBeneficiaria entidad, String nombreEntidad) {
    donacion.asignarA(entidad);
    if (notificador != null) notificador.donacionAsignada(donacion);
    if (logisticaGrpcClient == null) return;

    try {
      String origen = "DEPOSITO_CENTRAL"; // O el origen que maneje el sistema de donaciones
      String destino = entidad.getDireccion() != null ? entidad.getDireccion() : entidad.getRazonSocial();

      TrazarRutaResponse response = logisticaGrpcClient.trazarRuta(
          String.valueOf(donacion.getId()),
          origen,
          destino
      );

      if (!response.getExito()) {
        throw new IllegalStateException("Error reportado por gRPC logística: " + response.getMensaje());
      }
    } catch (Exception e) {
      throw new IllegalStateException("No se pudo informar la asignación a logística vía gRPC", e);
    }
  }

  public void limpiarSugerencias() {
    this.sugerenciaRepository.limpiar();
  }

  private SugerenciaAsignacion obtenerSugerenciaPorDonacion(Long idDonacion) {
    return sugerenciaRepository.buscarPorID(idDonacion)
        .orElseThrow(() -> new IllegalArgumentException("No existe una sugerencia para la donacion especificada"));
  }

  public Map<String, Object> asignaciones(Context ctx) {
    Map<String, Object> model = new HashMap<>();
    model.put("titulo", "Asignaciones pendientes");
    model.put("rol", "admin");
    model.put("seccion", "asignaciones");
    model.put("usuario", DatosMock.admin());   // MOCK
    model.put("ultimaEjecucion", "hoy 03:00");   // MOCK
    model.put("sugerencias", DatosMock.sugerencias());   // MOCK
    model.put("ejecutado", ctx.queryParam("ejecutado") != null);
    model.put("asignada", ctx.queryParam("asignada"));
    return model;
  }

  public Map<String, Object> asignacion(Context ctx) {
    String id = ctx.pathParam("id");

    Map<String, Object> model = new HashMap<>();
    model.put("titulo", "Elegir entidad final");
    model.put("rol", "admin");
    model.put("seccion", "asignaciones");
    model.put("usuario", DatosMock.admin());   // MOCK

    model.put("donacion", DatosMock.sugerencias().stream()
        .filter(s -> s.get("id").equals(id))
        .findFirst()
        .orElseThrow(NotFoundResponse::new));   // MOCK
    model.putAll(DatosMock.rankingsDeSugerencia(id));   // MOCK
    return model;
  }

  public void confirmarDestino(Context ctx) {
    ctx.redirect("/admin/asignaciones?asignada=" + ctx.pathParam("id"));
  }

  public void ejecutarAlgoritmosAhora(Context ctx) {
    ctx.redirect("/admin/asignaciones?ejecutado");
  }
}
