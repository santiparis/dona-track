package donaciones.web.controllers;

import donaciones.web.DatosMock;
import io.javalin.http.Context;

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
    model.put("categorias", DatosMock.categoriasConSubcategorias());   // MOCK
    return model;
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

  public Map<String, Object> seguimiento() {
    Map<String, Object> model = new HashMap<>();
    model.put("titulo", "Seguimiento de entregas");
    model.put("rol", "entidad");
    model.put("seccion", "seguimiento");
    Map<String, Object> entidad = DatosMock.entidad();   // MOCK
    model.put("usuario", entidad);
    model.put("subtitulo", "Camiones en camino con donaciones para tu entidad.");
    model.put("textoMapa", "Un marcador por camión y la ubicación de tu entidad");
    // Las ubicaciones vienen de logistica-service por HTTP (endpoint a definir en LogisticaAPICalls):
    // List<UbicacionCamionDTO> camiones = logisticaAPICalls.camionesConDestino(idEntidad).execute().body();
    model.put("camiones", DatosMock.camionesEnRuta().stream()
        .filter(c -> c.get("destino").equals(entidad.get("nombre")))
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
