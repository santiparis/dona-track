package donaciones.controller;

import io.javalin.http.Context;
import donaciones.domain.donante.Usuario;
import donaciones.repository.UsuarioRepository;
import donaciones.web.SesionUsuario;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class SesionController {
  private final UsuarioRepository usuarios;

  public SesionController(UsuarioRepository usuarios) {
    this.usuarios = usuarios;
  }

  public Map<String, Object> login() {
    Map<String, Object> model = new HashMap<>();
    model.put("titulo", "Iniciar sesión");
    model.put("seccion", "login");
    return model;
  }

  public void ingresar(Context ctx) {
    String nombreUsuario = Objects.requireNonNullElse(ctx.formParam("usuario"), "").trim();
    String password = ctx.formParam("password");
    Usuario usuario = nombreUsuario.isEmpty() ? null : usuarios.buscarPorNombreUsuario(nombreUsuario).orElse(null);
    if (usuario == null || !usuario.validarPassword(password) || usuario.getRol() == null) {
      Map<String, Object> model = login();
      model.put("error", "Usuario o contraseña incorrectos.");
      model.put("nombreUsuario", nombreUsuario);
      ctx.status(401).render("sesion/login", model);
      return;
    }

    SesionUsuario.iniciar(ctx, usuario);
    String destino = switch (usuario.getRol()) {
      case DONANTE -> "/donante/donaciones";
      case ENTIDAD -> "/entidad/necesidades";
      case ADMINISTRADORA -> "/admin/donaciones";
    };
    ctx.redirect(destino, io.javalin.http.HttpStatus.SEE_OTHER);
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
