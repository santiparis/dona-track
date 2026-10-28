package donaciones.web.controllers;

import donaciones.web.DatosMock;
import io.javalin.http.Context;
import io.javalin.http.NotFoundResponse;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class EntidadController {

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
    model.put("categorias", DatosMock.categorias().stream()
        .map(c -> Map.of("texto", c.get("texto"), "subcategorias", DatosMock.subcategorias(c.get("valor"))))
        .toList());   // MOCK
    return model;
  }

  public Map<String, Object> donaciones(Context ctx) {
    // El filtro viaja en la URL: ?estado=...
    String estado = Objects.requireNonNullElse(ctx.queryParam("estado"), "");

    Map<String, Object> model = new HashMap<>();
    model.put("titulo", "Donaciones asignadas");
    model.put("rol", "entidad");
    model.put("seccion", "donaciones");
    model.put("usuario", DatosMock.entidad());   // MOCK

    // List<Donacion> donaciones = donacionRepository.obtenerTodas().stream().filter(d -> d.getEntidadBeneficiaria() == entidad).toList();
    model.put("donaciones", DatosMock.donacionesAsignadas().stream()
        .filter(d -> estado.isEmpty() || estado.equals(d.get("estado")))
        .toList());   // MOCK
    model.put("estadoElegido", estado);
    // A la entidad solo le llegan donaciones ya asignadas: no tiene sentido filtrar por En depósito ni Vencida
    model.put("estados", DatosMock.estados().stream()
        .filter(e -> !List.of("EN_DEPOSITO", "VENCIDA").contains(e.get("valor")))
        .toList());   // MOCK
    return model;
  }

  public Map<String, Object> recepcion(Context ctx) {
    String id = ctx.pathParam("id");

    Map<String, Object> model = new HashMap<>();
    model.put("titulo", "Confirmar recepción");
    model.put("rol", "entidad");
    model.put("seccion", "donaciones");
    model.put("usuario", DatosMock.entidad());   // MOCK

    // Donacion donacion = donacionRepository.buscarPorId(Long.valueOf(id)).orElseThrow(NotFoundResponse::new);
    model.put("donacion", DatosMock.donacionesAsignadas().stream()
        .filter(d -> d.get("id").equals(id))
        .findFirst()
        .orElseThrow(NotFoundResponse::new));   // MOCK
    return model;
  }

  public void confirmarRecepcion(Context ctx) {
    // Sin persistencia todavía: acá la donación pasa a Entregada y se guardan las fotos de ctx.uploadedFiles("fotos")
    ctx.redirect("/entidad/donaciones");
  }

  public void informarNoRecibida(Context ctx) {
    // Sin persistencia todavía: acá la entrega se marca No recibida con el motivo, para que la revise la administración
    ctx.redirect("/entidad/donaciones");
  }

  public Map<String, Object> notificaciones() {
    Map<String, Object> model = new HashMap<>();
    model.put("titulo", "Notificaciones");
    model.put("rol", "entidad");
    model.put("seccion", "notificaciones");
    model.put("usuario", DatosMock.entidad());   // MOCK
    model.put("aviso", "También llegan a los correos de los representantes.");
    // List<Notificacion> notificaciones = notificacionRepository.obtenerTodas().stream().filter(n -> n.getReceptorId().equals(idEntidad)).toList();
    model.put("notificaciones", DatosMock.notificaciones().stream()
        .filter(n -> "entidad".equals(n.get("destinatario")))
        .toList());   // MOCK
    return model;
  }

  public void crearNecesidad(Context ctx) {
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
