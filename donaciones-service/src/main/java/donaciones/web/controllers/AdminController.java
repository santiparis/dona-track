package donaciones.web.controllers;

import donaciones.web.DatosMock;
import io.javalin.http.Context;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdminController {

  public Map<String, Object> camiones(Context ctx) {
    Map<String, Object> model = new HashMap<>();
    model.put("titulo", "Camiones");
    model.put("rol", "admin");
    model.put("seccion", "camiones");
    model.put("usuario", DatosMock.admin());   // MOCK

    // Los camiones son de logistica-service: se piden por HTTP a GET /api/camiones (falta sumarlo a LogisticaAPICalls)
    // List<CamionDTO> camiones = logisticaAPICalls.getCamiones().execute().body();
    List<Map<String, String>> camiones = DatosMock.camiones();   // MOCK
    model.put("camiones", camiones);

    // ?editar={id} carga ese camión en el formulario; si no, el formulario queda vacío para dar de alta
    String idEditar = ctx.queryParam("editar");
    model.put("camion", camiones.stream()
        .filter(c -> c.get("id").equals(idEditar))
        .findFirst()
        .orElse(Map.of()));
    return model;
  }

  public void crearCamion(Context ctx) {
    // Sin conexión todavía: acá va logisticaAPICalls.crearCamion(...) contra POST /api/camiones
    ctx.redirect("/admin/camiones");
  }

  public void modificarCamion(Context ctx) {
    // Sin conexión todavía: acá va logisticaAPICalls.modificarCamion(id, ...) contra PUT /api/camiones/{id}
    ctx.redirect("/admin/camiones");
  }

  public void eliminarCamion(Context ctx) {
    // Sin conexión todavía: acá va logisticaAPICalls.eliminarCamion(id) contra DELETE /api/camiones/{id}
    ctx.redirect("/admin/camiones");
  }
}
