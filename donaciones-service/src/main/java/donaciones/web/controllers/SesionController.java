package donaciones.web.controllers;

import io.javalin.http.Context;

import java.util.HashMap;
import java.util.Map;

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
}
