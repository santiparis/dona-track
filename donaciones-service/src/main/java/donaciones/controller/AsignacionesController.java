package donaciones.controller;

import donaciones.domain.Donacion;
import donaciones.domain.EstadoDonacion;
import donaciones.domain.EntidadBeneficiaria;
import donaciones.domain.algoritmos.CompatibilidadSemantica;
import donaciones.domain.algoritmos.OrganizadorAsignaciones;
import donaciones.domain.algoritmos.PrioridadSubAtendidos;
import donaciones.domain.algoritmos.SugerenciaAsignacion;
import donaciones.dto.AsignacionRequestDTO;
import donaciones.repository.DonacionRepository;
import donaciones.repository.EntidadBeneficiariaRepository;
import donaciones.repository.SugerenciaAsignacionRepository;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;

import java.util.ArrayList;
import java.util.List;

public class AsignacionesController {

  private final DonacionRepository donacionRepository;
  private final EntidadBeneficiariaRepository entidadRepository;
  private final SugerenciaAsignacionRepository sugerenciaRepository;
  private final OrganizadorAsignaciones organizadorAsignaciones;

  public AsignacionesController(
      DonacionRepository donacionRepository,
      EntidadBeneficiariaRepository entidadRepository,
      SugerenciaAsignacionRepository sugerenciaRepository
  ) {
    this.donacionRepository = donacionRepository;
    this.entidadRepository = entidadRepository;
    this.sugerenciaRepository = sugerenciaRepository;
    this.organizadorAsignaciones = new OrganizadorAsignaciones(List.of(new CompatibilidadSemantica(), new PrioridadSubAtendidos()));
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

    SugerenciaAsignacion sugerencia = this.sugerenciaRepository.buscarPorID(idSugerencia).orElseThrow();

    boolean entidadEnSugerencias = sugerencia.getEntidadesPorAlgoritmo()
        .values()
        .stream()
        .flatMap(List::stream)
        .anyMatch(entidad -> entidad.getId() != null && entidad.getId().equals(dto.idEntidad()));

    if (!entidadEnSugerencias) {
      throw new IllegalArgumentException("La entidad seleccionada no aparece en ninguna de las listas generadas por los algoritmos");
    }

    EntidadBeneficiaria entidad = this.entidadRepository.buscarPorId(dto.idEntidad())
        .orElseThrow(() -> new IllegalArgumentException("No existe la entidad especificada"));

    sugerencia.getDonacion().asignarA(entidad);

    this.sugerenciaRepository.eliminar(sugerencia);

    // TODO: Pegarle al endpoint de logistica para que guarde la donacion para futura entrega
  }

  public void limpiarSugerencias() {
    this.sugerenciaRepository.limpiar();
  }

  private SugerenciaAsignacion obtenerSugerenciaPorDonacion(Long idDonacion) {
    return sugerenciaRepository.buscarPorID(idDonacion)
        .orElseThrow(() -> new IllegalArgumentException("No existe una sugerencia para la donacion especificada"));
  }
}
