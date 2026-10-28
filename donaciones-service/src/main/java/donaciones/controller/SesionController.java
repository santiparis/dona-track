package donaciones.controller;

import io.javalin.http.Context;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class SesionController {

  public Map<String, Object> login() {
    Map<String, Object> model = new HashMap<>();
    model.put("titulo", "Iniciar sesión");
    model.put("seccion", "login");
    return model;
  }

  public void ingresar(Context ctx) {
    // Sin sesión todavía: acá va a ir la validación de usuario y contraseña
    ctx.redirect("/");
  }

  public Map<String, Object> registro(Context ctx) {
    Map<String, Object> model = new HashMap<>();
    model.put("titulo", "Registro");
    model.put("seccion", "registro");
    // La pestaña elegida viaja en la URL: /usuarios/nuevo?tipo=HUMANA|JURIDICA|ENTIDAD
    model.put("tipo", Objects.requireNonNullElse(ctx.queryParam("tipo"), "HUMANA"));
    return model;
  }

  public void registrar(Context ctx) {
    // Sin persistencia todavía: acá va a ir el alta del donante o la entidad
    ctx.redirect("/login");
  }
}
