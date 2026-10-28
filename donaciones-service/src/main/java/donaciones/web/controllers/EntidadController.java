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
    model.put("categorias", DatosMock.categorias().stream()
        .map(c -> Map.of("texto", c.get("texto"), "subcategorias", DatosMock.subcategorias(c.get("valor"))))
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
