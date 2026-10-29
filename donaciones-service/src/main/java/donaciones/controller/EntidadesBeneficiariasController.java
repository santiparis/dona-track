package donaciones.controller;

import donaciones.domain.*;
import donaciones.dto.EntidadBeneficiariaDTO;
import donaciones.dto.EntidadBeneficiariaPatchDTO;
import donaciones.dto.NecesidadDTO;
import donaciones.repository.EntidadBeneficiariaRepository;
import donaciones.web.DatosMock;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
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
      EntidadBeneficiaria entidad = buscarEntidad(id);
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
      EntidadBeneficiaria entidad = buscarEntidad(id);
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
      buscarEntidad(id);
      entidadesRepository.eliminarPorId(id);
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
      EntidadBeneficiaria entidad = buscarEntidad(id);
      entidad.registrarNecesidad(crearNecesidad(dto));
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
      EntidadBeneficiaria entidad = buscarEntidad(id);
      buscarNecesidad(entidad, idNecesidad);
      entidad.actualizarNecesidadPorId(idNecesidad, crearNecesidad(dto));
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
      EntidadBeneficiaria entidad = buscarEntidad(idEntidad);
      buscarNecesidad(entidad, idNecesidad);
      entidad.actualizarNecesidadPorId(idNecesidad, crearNecesidad(dto));
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
      EntidadBeneficiaria entidad = buscarEntidad(id);
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
    return new Necesidad(dto.descripcion(), parsearRenovacion(dto), cantidades);
  }

  private PoliticaDeRenovacion parsearRenovacion(NecesidadDTO dto) {
    if (!dto.renovacion()) return new SinRenovacion();
    if (dto.fechaInicio() == null || dto.periodo() == null) {
      throw new IllegalArgumentException("Una necesidad renovable requiere fecha de inicio y período");
    }
    return new RenovacionPeriodica(LocalDate.parse(dto.fechaInicio()), parsearPeriodo(dto.periodo()));
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
    model.put("usuario", DatosMock.donante());   // MOCK

    // List<EntidadBeneficiaria> entidades = entidadRepo.buscar(busqueda, subcategoria);
    model.put("entidades", DatosMock.entidadesConNecesidades().stream()
        .filter(e -> ((String) e.get("nombre")).toLowerCase().contains(busqueda.toLowerCase()))
        .filter(e -> subcategoria.isEmpty() || ((List<?>) e.get("necesidades")).stream()
            .anyMatch(n -> subcategoria.equals(((Map<?, ?>) n).get("subcategoria"))))
        .toList());   // MOCK
    model.put("busqueda", busqueda);
    model.put("subcategoriaElegida", subcategoria);
    // Todas las subcategorías, de todas las categorías
    model.put("subcategorias", DatosMock.categorias().stream()
        .flatMap(c -> DatosMock.subcategorias(c.get("valor")).stream())
        .toList());   // MOCK
    return model;
  }

  public Map<String, Object> necesidades(Context ctx) {
    Map<String, Object> model = new HashMap<>();
    model.put("titulo", "Necesidades");
    model.put("rol", "entidad");
    model.put("seccion", "necesidades");
    model.put("usuario", DatosMock.entidad());   // MOCK

    // List<Necesidad> necesidades = entidadRepo.buscarPorId(idEntidad).orElseThrow().getNecesidades();
    List<Map<String, String>> necesidades = DatosMock.necesidadesDeLaEntidad();   // MOCK
    model.put("necesidades", necesidades);

    // ?editar={id} carga esa necesidad en el formulario; si no, el formulario queda vacío para dar de alta
    String idEditar = ctx.queryParam("editar");
    Map<String, String> enEdicion = necesidades.stream()
        .filter(n -> n.get("id").equals(idEditar))
        .findFirst()
        .orElse(Map.of());
    model.put("necesidad", enEdicion);

    // La pestaña viaja en la URL (?tipo=EXTRAORDINARIA|RECURRENTE); al editar arranca en el tipo de la necesidad
    model.put("tipo", Objects.requireNonNullElse(ctx.queryParam("tipo"), enEdicion.getOrDefault("tipo", "EXTRAORDINARIA")));

    // Subcategorías agrupadas por categoría para el <select> con <optgroup>
    model.put("categorias", DatosMock.categoriasConSubcategorias());   // MOCK
    return model;
  }

  public void registrarNecesidad(Context ctx) {
    // Sin persistencia todavía: acá se arma la Necesidad con los datos del form y se registra en la entidad
    ctx.redirect("/entidad/necesidades");
  }

  public void modificarNecesidad(Context ctx) {
    // Sin persistencia todavía: acá se busca la necesidad ctx.pathParam("id") y se actualiza con los datos del form
    ctx.redirect("/entidad/necesidades");
  }

  public void eliminarNecesidad(Context ctx) {
    // Sin persistencia todavía: acá va la baja de la necesidad ctx.pathParam("id")
    ctx.redirect("/entidad/necesidades");
  }
}
