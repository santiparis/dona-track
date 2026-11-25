package donaciones.controller;

import donaciones.domain.*;
import donaciones.dto.EntidadBeneficiariaDTO;
import donaciones.dto.EntidadBeneficiariaPatchDTO;
import donaciones.dto.NecesidadDTO;
import donaciones.repository.EntidadBeneficiariaRepository;
import donaciones.web.SesionUsuario;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import io.javalin.http.NotFoundResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class EntidadesBeneficiariasController {
  private static final Logger logger = LoggerFactory.getLogger(EntidadesBeneficiariasController.class);
  private final EntidadBeneficiariaRepository entidadesRepository;

  public EntidadesBeneficiariasController(EntidadBeneficiariaRepository entidadesRepository) {
    this.entidadesRepository = entidadesRepository;
  }

  public void getEntidadesBeneficiarias(Context ctx) {
    try {
      ctx.json(entidadesRepository.obtenerTodas());
    } catch (RuntimeException e) {
      logger.error("Error al listar entidades beneficiarias", e);
      ctx.status(HttpStatus.INTERNAL_SERVER_ERROR).result("Error al listar entidades beneficiarias: " + e.getMessage());
    }
  }

  public void getEntidadBeneficiaria(Context ctx) {
    try {
      Long id = Long.parseLong(ctx.pathParam("id"));
      ctx.json(buscarEntidad(id));
    } catch (RuntimeException e) {
      logger.error("Error al obtener entidad beneficiaria", e);
      manejarExcepcion(ctx, e);
    }
  }

  public void postEntidadBeneficiaria(Context ctx) {
    try {
      EntidadBeneficiariaDTO dto = ctx.bodyAsClass(EntidadBeneficiariaDTO.class);
      entidadesRepository.guardar(new EntidadBeneficiaria(dto.razonSocial(), dto.direccion(), dto.telefono(), dto.correosRepresentantes()));
      ctx.status(HttpStatus.CREATED).result("Entidad Beneficiaria recibida y guardada");
    } catch (IllegalArgumentException e) {
      logger.warn("Error de validacion al crear entidad beneficiaria: {}", e.getMessage());
      ctx.status(HttpStatus.BAD_REQUEST).result("Error: " + e.getMessage());
    } catch (RuntimeException e) {
      logger.error("Error inesperado al crear entidad beneficiaria", e);
      ctx.status(HttpStatus.INTERNAL_SERVER_ERROR).result("Error al crear entidad beneficiaria: " + e.getMessage());
    }
  }

  public void putEntidadBeneficiaria(Context ctx) {
    try {
      Long id = Long.parseLong(ctx.pathParam("id"));
      EntidadBeneficiariaDTO dto = ctx.bodyAsClass(EntidadBeneficiariaDTO.class);
      EntidadBeneficiaria entidad = buscarEntidadParaActualizar(id);
      entidad.actualizarDatos(dto.razonSocial(), dto.direccion(), dto.telefono(), dto.correosRepresentantes());
      entidadesRepository.guardar(entidad);
      ctx.result("Entidad beneficiaria actualizada");
    } catch (IllegalArgumentException e) {
      logger.warn("Error al actualizar entidad beneficiaria: {}", e.getMessage());
      ctx.status(HttpStatus.NOT_FOUND).result(e.getMessage());
    } catch (RuntimeException e) {
      logger.error("Error inesperado al actualizar entidad beneficiaria", e);
      ctx.status(HttpStatus.INTERNAL_SERVER_ERROR).result("Error al actualizar entidad beneficiaria: " + e.getMessage());
    }
  }

  public void patchEntidadBeneficiaria(Context ctx) {
    try {
      Long id = Long.parseLong(ctx.pathParam("id"));
      EntidadBeneficiariaPatchDTO dto = ctx.bodyAsClass(EntidadBeneficiariaPatchDTO.class);
      EntidadBeneficiaria entidad = buscarEntidadParaActualizar(id);
      entidad.actualizarDatos(dto.razonSocial(), dto.direccion(), dto.telefono(), dto.correosRepresentantes());
      entidadesRepository.guardar(entidad);
      ctx.result("Entidad beneficiaria actualizada parcialmente");
    } catch (IllegalArgumentException e) {
      logger.warn("Error al actualizar parcialmente entidad beneficiaria: {}", e.getMessage());
      ctx.status(HttpStatus.NOT_FOUND).result(e.getMessage());
    } catch (RuntimeException e) {
      logger.error("Error inesperado al actualizar parcialmente entidad beneficiaria", e);
      ctx.status(HttpStatus.INTERNAL_SERVER_ERROR).result("Error al actualizar entidad beneficiaria: " + e.getMessage());
    }
  }

  public void deleteEntidadBeneficiaria(Context ctx) {
    try {
      Long id = Long.parseLong(ctx.pathParam("id"));
      EntidadBeneficiaria entidad = buscarEntidadParaActualizar(id);
      entidadesRepository.eliminar(entidad);
      ctx.result("Entidad beneficiaria eliminada correctamente");
    } catch (IllegalArgumentException e) {
      logger.warn("Error al eliminar entidad beneficiaria: {}", e.getMessage());
      ctx.status(HttpStatus.NOT_FOUND).result(e.getMessage());
    } catch (RuntimeException e) {
      logger.error("Error inesperado al eliminar entidad beneficiaria", e);
      ctx.status(HttpStatus.INTERNAL_SERVER_ERROR).result("Error al eliminar entidad beneficiaria: " + e.getMessage());
    }
  }

  public void getNecesidades(Context ctx) {
    try {
      Long id = Long.parseLong(ctx.pathParam("id"));
      ctx.json(buscarEntidad(id).getNecesidades());
    } catch (RuntimeException e) {
      logger.error("Error al listar necesidades", e);
      manejarExcepcion(ctx, e);
    }
  }

  public void postNecesidades(Context ctx) {
    Long id = Long.parseLong(ctx.pathParam("id"));
    NecesidadDTO dto = ctx.bodyAsClass(NecesidadDTO.class);

    try {
      Necesidad necesidad = crearNecesidad(dto);
      EntidadBeneficiaria entidad = buscarEntidadParaActualizar(id);
      entidad.registrarNecesidad(necesidad);
      entidadesRepository.guardar(entidad);
      ctx.status(HttpStatus.CREATED).result("Necesidad guardada correctamente");
    } catch (RuntimeException e) {
      manejarExcepcion(ctx, e);
    }
  }

  public void putNecesidad(Context ctx) {
    Long id = Long.parseLong(ctx.pathParam("id"));
    Long idNecesidad = Long.parseLong(ctx.pathParam("idNecesidad"));
    NecesidadDTO dto = ctx.bodyAsClass(NecesidadDTO.class);

    try {
      Necesidad necesidad = crearNecesidad(dto);
      EntidadBeneficiaria entidad = buscarEntidadParaActualizar(id);
      buscarNecesidad(entidad, idNecesidad);
      entidad.actualizarNecesidadPorId(idNecesidad, necesidad);
      entidadesRepository.guardar(entidad);
      ctx.result("Necesidad actualizada correctamente");
    } catch (RuntimeException e) {
      manejarExcepcion(ctx, e);
    }
  }

  public void patchNecesidad(Context ctx) {
    Long idEntidad = Long.parseLong(ctx.pathParam("id"));
    Long idNecesidad = Long.parseLong(ctx.pathParam("idNecesidad"));
    NecesidadDTO dto = ctx.bodyAsClass(NecesidadDTO.class);

    try {
      Necesidad necesidad = crearNecesidad(dto);
      EntidadBeneficiaria entidad = buscarEntidadParaActualizar(idEntidad);
      buscarNecesidad(entidad, idNecesidad);
      entidad.actualizarNecesidadPorId(idNecesidad, necesidad);
      entidadesRepository.guardar(entidad);
      ctx.result("Necesidad actualizada parcialmente");
    } catch (RuntimeException e) {
      manejarExcepcion(ctx, e);
    }
  }

  public void deleteNecesidad(Context ctx) {
    Long id = Long.parseLong(ctx.pathParam("id"));
    Long idNecesidad = Long.parseLong(ctx.pathParam("idNecesidad"));

    try {
      EntidadBeneficiaria entidad = buscarEntidadParaActualizar(id);
      buscarNecesidad(entidad, idNecesidad);
      entidad.eliminarNecesidadPorId(idNecesidad);
      entidadesRepository.guardar(entidad);
      ctx.result("Necesidad eliminada correctamente");
    } catch (RuntimeException e) {
      manejarExcepcion(ctx, e);
    }
  }

  private void manejarExcepcion(Context ctx, RuntimeException e) {
    if (e instanceof IllegalArgumentException) {
      ctx.status(HttpStatus.BAD_REQUEST).result("Error: " + e.getMessage());
      return;
    }

    ctx.status(HttpStatus.NOT_FOUND).result("Error: " + e.getMessage());
  }

  private EntidadBeneficiaria buscarEntidad(Long id) {
    return entidadesRepository.buscarPorId(id)
        .orElseThrow(() -> new IllegalArgumentException("No se encontró la entidad beneficiaria"));
  }

  private EntidadBeneficiaria buscarEntidadParaActualizar(Long id) {
    return entidadesRepository.buscarPorIdParaActualizar(id)
        .orElseThrow(() -> new IllegalArgumentException("No se encontró la entidad beneficiaria"));
  }

  private Necesidad buscarNecesidad(EntidadBeneficiaria entidad, Long idNecesidad) {
    return entidad.getNecesidades().stream()
        .filter(n -> n.getId() != null && n.getId().equals(idNecesidad))
        .findFirst()
        .orElseThrow(() -> new IllegalArgumentException("No se encontró la necesidad"));
  }

  /** Adaptación del DTO HTTP al dominio. */
  private Necesidad crearNecesidad(NecesidadDTO dto) {
    Map<Subcategoria, Integer> cantidades = new HashMap<>();
    dto.cantidadesRequeridas().forEach((subcategoria, cantidad) -> cantidades.put(parsearSubcategoria(subcategoria), cantidad));
    boolean renovacion = dto.renovacion();
    // La fecha se guarda en los dos tipos (si viene); el período, solo en la recurrente
    LocalDate fechaInicio = dto.fechaInicio() == null ? null : LocalDate.parse(dto.fechaInicio());
    Periodo periodo = null;
    if (renovacion) {
      if (dto.fechaInicio() == null || dto.periodo() == null) {
        throw new IllegalArgumentException("Una necesidad renovable requiere fecha de inicio y período");
      }
      periodo = parsearPeriodo(dto.periodo());
    }
    return new Necesidad(dto.descripcion(), renovacion, fechaInicio, periodo, cantidades);
  }

  private Subcategoria parsearSubcategoria(String subcategoria) {
    try { return Subcategoria.valueOf(subcategoria.toUpperCase()); }
    catch (IllegalArgumentException e) { throw new IllegalArgumentException("Subcategoria inexistente", e); }
  }

  private Periodo parsearPeriodo(String periodo) {
    try { return Periodo.valueOf(periodo.toUpperCase()); }
    catch (IllegalArgumentException e) { throw new IllegalArgumentException("Periodo incorrecto", e); }
  }

  public Map<String, Object> entidades(Context ctx) {
    // La búsqueda viaja en la URL: ?q=...&subcategoria=...
    String busqueda = Objects.requireNonNullElse(ctx.queryParam("q"), "");
    String subcategoria = Objects.requireNonNullElse(ctx.queryParam("subcategoria"), "");

    Map<String, Object> model = new HashMap<>();
    model.put("titulo", "Entidades beneficiarias");
    model.put("rol", "donante");
    model.put("seccion", "entidades");

    model.put("entidades", entidadesRepository.obtenerTodas().stream()
        .filter(e -> e.getRazonSocial().toLowerCase().contains(busqueda.toLowerCase()))
        .filter(e -> subcategoria.isEmpty() || e.getNecesidades().stream()
            .anyMatch(n -> n.getCantidadesRequeridas().containsKey(Subcategoria.valueOf(subcategoria))))
        .map(EntidadesBeneficiariasController::entidadConNecesidades)
        .toList());
    model.put("busqueda", busqueda);
    model.put("subcategoriaElegida", subcategoria);
    // Todas las subcategorías, de todas las categorías
    model.put("subcategorias", Arrays.stream(Subcategoria.values())
        .map(s -> Map.of("valor", s.name(), "texto", s.nombre()))
        .toList());
    return model;
  }

  // La entidad como la muestran el listado y el detalle: una necesidad por subcategoría, con su avance
  private static Map<String, Object> entidadConNecesidades(EntidadBeneficiaria entidad) {
    List<Map<String, String>> necesidades = new ArrayList<>();
    for (Necesidad necesidad : entidad.getNecesidades()) {
      necesidad.getCantidadesRequeridas().forEach((subcategoria, requerido) -> {
        int suplido = necesidad.getCantidadesSuplidas().getOrDefault(subcategoria, 0);
        String detalle = suplido + " de " + requerido + " " + subcategoria.unidad();
        if (necesidad.getPeriodo() == Periodo.SEMANAL) {
          detalle += " esta semana";
        } else if (necesidad.getPeriodo() == Periodo.MENSUAL) {
          detalle += " este mes";
        }
        if (necesidad.getDescripcion() != null) {
          detalle += " · " + necesidad.getDescripcion();
        }
        necesidades.add(Map.of(
            "subcategoria", subcategoria.name(),
            "subcategoriaTexto", subcategoria.nombre(),
            "tipo", necesidad.getPeriodo() == null ? "Extraordinaria" : "Recurrente · " + necesidad.getPeriodo().name().toLowerCase(),
            "porcentaje", String.valueOf(suplido * 100 / requerido),
            "detalle", detalle));
      });
    }
    return Map.of(
        "id", entidad.getId().toString(),
        "nombre", entidad.getRazonSocial(),
        "direccion", entidad.getDireccion(),
        "telefono", entidad.getTelefono(),
        "correos", entidad.getCorreosRepresentantes(),
        "necesidades", necesidades);
  }

  public Map<String, Object> entidad(Context ctx) {
    String id = ctx.pathParam("id");

    Map<String, Object> model = new HashMap<>();
    model.put("rol", "donante");
    model.put("seccion", "entidades");

    Map<String, Object> entidad = entidadConNecesidades(entidadesRepository.buscarPorId(Long.valueOf(id))
        .orElseThrow(NotFoundResponse::new));
    model.put("entidad", entidad);
    model.put("titulo", entidad.get("nombre"));
    return model;
  }

  public Map<String, Object> necesidades(Context ctx) {
    Map<String, Object> model = new HashMap<>();
    model.put("titulo", "Mis necesidades");
    model.put("rol", "entidad");
    model.put("seccion", "necesidades");

    List<Map<String, String>> necesidades = entidadDeLaSesion(ctx).getNecesidades().stream()
        .flatMap(necesidad -> necesidad.getCantidadesRequeridas().entrySet().stream()
            .map(cantidad -> necesidadParaVista(necesidad, cantidad.getKey(), cantidad.getValue())))
        .toList();
    model.put("necesidades", necesidades);
    return model;
  }

  public void registrarNecesidad(Context ctx) {
    // Lo mismo que POST /api/entidades-beneficiarias/{id}/necesidades, con la entidad de la sesión
    NecesidadDTO dto = new NecesidadDTO(
        ctx.formParam("descripcion"),
        "RECURRENTE".equals(ctx.formParam("tipo")),
        Map.of(ctx.formParam("subcategoria"), ctx.formParamAsClass("cantidad", Integer.class).get()),
        ctx.formParam("fechaInicio"),
        ctx.formParam("periodo"));
    EntidadBeneficiaria entidad = entidadDeLaSesion(ctx);
    entidad.registrarNecesidad(crearNecesidad(dto));
    entidadesRepository.guardar(entidad);
    ctx.sessionAttribute("aviso", "Necesidad registrada.");
    ctx.redirect("/entidad/necesidades");
  }

  public void modificarNecesidad(Context ctx) {
    verificarNecesidadPropia(ctx);
    // Lo mismo que PUT /api/entidades-beneficiarias/{id}/necesidades/{idNecesidad}, con la entidad de la sesión
    NecesidadDTO dto = new NecesidadDTO(
        ctx.formParam("descripcion"),
        "RECURRENTE".equals(ctx.formParam("tipo")),
        Map.of(ctx.formParam("subcategoria"), ctx.formParamAsClass("cantidad", Integer.class).get()),
        ctx.formParam("fechaInicio"),
        ctx.formParam("periodo"));
    EntidadBeneficiaria entidad = entidadDeLaSesion(ctx);
    entidad.actualizarNecesidadPorId(Long.valueOf(ctx.pathParam("id")), crearNecesidad(dto));
    entidadesRepository.guardar(entidad);
    ctx.sessionAttribute("aviso", "Necesidad actualizada.");
    ctx.redirect("/entidad/necesidades");
  }

  public void eliminarNecesidad(Context ctx) {
    verificarNecesidadPropia(ctx);
    // Lo mismo que DELETE /api/entidades-beneficiarias/{id}/necesidades/{idNecesidad}, con la entidad de la sesión
    EntidadBeneficiaria entidad = entidadDeLaSesion(ctx);
    entidad.eliminarNecesidadPorId(Long.valueOf(ctx.pathParam("id")));
    entidadesRepository.guardar(entidad);
    ctx.sessionAttribute("aviso", "Necesidad eliminada.");
    ctx.redirect("/entidad/necesidades");
  }

  private EntidadBeneficiaria entidadDeLaSesion(Context ctx) {
    return entidadesRepository.buscarPorUsuario(ctx.sessionAttribute(SesionUsuario.USUARIO_ID))
        .orElseThrow(io.javalin.http.NotFoundResponse::new);
  }

  private void verificarNecesidadPropia(Context ctx) {
    String id = ctx.pathParam("id");
    if (entidadDeLaSesion(ctx).getNecesidades().stream().noneMatch(n -> n.getId().toString().equals(id))) {
      throw new io.javalin.http.NotFoundResponse();
    }
  }

  private Map<String, String> necesidadParaVista(Necesidad necesidad, Subcategoria subcategoria, int cantidad) {
    int suplida = necesidad.getCantidadesSuplidas().getOrDefault(subcategoria, 0);
    boolean recurrente = necesidad.getPeriodo() != null;
    // Map.ofEntries porque Map.of admite hasta 10 pares
    return Map.ofEntries(
        Map.entry("id", necesidad.getId().toString()),
        Map.entry("descripcion", Objects.toString(necesidad.getDescripcion(), "")),
        Map.entry("subcategoria", subcategoria.name()),
        Map.entry("subcategoriaTexto", subcategoria.nombre()),
        Map.entry("cantidad", Integer.toString(cantidad)),
        Map.entry("tipo", recurrente ? "RECURRENTE" : "EXTRAORDINARIA"),
        Map.entry("tipoTexto", recurrente ? "Recurrente" : "Extraordinaria"),
        Map.entry("periodo", Objects.toString(necesidad.getPeriodo(), "")),
        Map.entry("avance", suplida + " / " + cantidad + " " + subcategoria.unidad()),
        Map.entry("porcentaje", Long.toString(cantidad > 0 ? Math.min(100, Math.round(100.0 * suplida / cantidad)) : 0)),
        // Para el formulario de edición: la fecha cargada y la cantidad mínima (no menos de lo ya recibido)
        Map.entry("fechaInicio", Objects.toString(necesidad.getFechaInicio(), "")),
        Map.entry("minimo", Integer.toString(Math.max(1, suplida))));
  }

  // Registrar necesidad, separada de Mis necesidades. Con ?editar={id} el formulario edita esa necesidad
  public Map<String, Object> formularioNecesidad(Context ctx) {
    Map<String, Object> model = new HashMap<>();
    model.put("rol", "entidad");
    model.put("seccion", "necesidad-nueva");

    List<Map<String, String>> necesidades = entidadDeLaSesion(ctx).getNecesidades().stream()
        .flatMap(necesidad -> necesidad.getCantidadesRequeridas().entrySet().stream()
            .map(cantidad -> necesidadParaVista(necesidad, cantidad.getKey(), cantidad.getValue())))
        .toList();

    // ?editar={id} carga esa necesidad en el formulario; si no, el formulario queda vacío para dar de alta
    String idEditar = ctx.queryParam("editar");
    Map<String, String> enEdicion = necesidades.stream()
        .filter(n -> n.get("id").equals(idEditar))
        .findFirst()
        .orElseGet(() -> {
          if (idEditar != null) throw new io.javalin.http.NotFoundResponse();
          return Map.of();
        });
    model.put("necesidad", enEdicion);
    model.put("titulo", enEdicion.isEmpty() ? "Registrar necesidad" : "Editar necesidad");

    // El tipo se elige en el mismo formulario; al editar arranca en el tipo de la necesidad
    model.put("tipo", enEdicion.getOrDefault("tipo", "EXTRAORDINARIA"));

    // Subcategorías agrupadas por categoría para el <select> con <optgroup>
    model.put("categorias", Arrays.stream(Categoria.values())
        .map(c -> Map.of(
            "texto", c.nombre(),
            "subcategorias", Arrays.stream(Subcategoria.values())
                .filter(s -> s.getCategoria() == c)
                .map(s -> Map.of("valor", s.name(), "texto", s.nombre(), "unidad", s.unidad()))
                .toList()))
        .toList());
    return model;
  }
}
