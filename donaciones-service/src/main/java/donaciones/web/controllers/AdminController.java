package donaciones.web.controllers;

import donaciones.web.DatosMock;
import io.javalin.http.Context;

import java.util.HashMap;
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
}
