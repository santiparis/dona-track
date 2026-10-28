package donaciones.web.controllers;

import donaciones.web.DatosMock;
import io.javalin.http.Context;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class AdminController {

  public Map<String, Object> donanteNuevo(Context ctx) {
    Map<String, Object> model = new HashMap<>();
    model.put("titulo", "Registrar donante");
    model.put("rol", "admin");
    model.put("seccion", "donante-nuevo");
    model.put("usuario", DatosMock.admin());   // MOCK
    // La pestaña viaja en la URL: /admin/donantes/nuevo?tipo=HUMANA|JURIDICA
    model.put("tipo", Objects.requireNonNullElse(ctx.queryParam("tipo"), "HUMANA"));
    return model;
  }

  public void registrarDonante(Context ctx) {
    // Sin persistencia todavía: acá se da de alta la persona y se le manda el mail de bienvenida
    // "Registrar y cargar donación" manda siguiente=donacion para seguir directo con la donación
    if ("donacion".equals(ctx.formParam("siguiente"))) {
      ctx.redirect("/admin/donaciones/nueva");
    } else {
      ctx.redirect("/admin/donantes/nuevo");
    }
  }

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

  public Map<String, Object> importacion(Context ctx) {
    Map<String, Object> model = new HashMap<>();
    model.put("titulo", "Importar donantes");
    model.put("rol", "admin");
    model.put("seccion", "importacion");
    model.put("usuario", DatosMock.admin());   // MOCK
    // Después de importar se vuelve con ?importado para mostrar el resultado
    if (ctx.queryParam("importado") != null) {
      model.put("resultado", DatosMock.resultadoImportacion());   // MOCK
    }
    return model;
  }

  public void importar(Context ctx) {
    // Sin conexión todavía: acá se guarda ctx.uploadedFile("archivo") en un archivo temporal y se llama al importador ya hecho
    // new ImportadorPersonas(repositorioPersonas).importarPersonasDesdeCSV(pathTemporal);
    ctx.redirect("/admin/importacion?importado");
  }
}
