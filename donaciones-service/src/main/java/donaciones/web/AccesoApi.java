package donaciones.web;

import donaciones.domain.donante.Rol;
import donaciones.domain.donante.Usuario;
import io.javalin.http.Context;

import java.util.Map;

/** La API REST requiere una sesión administradora. */
public class AccesoApi {

  public void verificar(Context ctx) {
    if (!ctx.matchedPath().startsWith("/api/")) return;
    ctx.header("Cache-Control", "no-store");

    Usuario usuario = SesionUsuario.actual(ctx);
    if (usuario == null) {
      ctx.status(401).json(Map.of("error", "Iniciá sesión para acceder a la API."));
      ctx.skipRemainingHandlers();
    } else if (usuario.getRol() != Rol.ADMINISTRADORA) {
      ctx.status(403).json(Map.of("error", "Esta operación requiere una cuenta administradora."));
      ctx.skipRemainingHandlers();
    }
  }

}
