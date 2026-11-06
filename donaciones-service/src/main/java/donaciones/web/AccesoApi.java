package donaciones.web;

import donaciones.domain.donante.Rol;
import donaciones.domain.donante.Usuario;
import io.javalin.http.Context;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Map;

/** La API requiere una administradora; logística solo puede informar estados. */
public class AccesoApi {
  private final String tokenInterno;

  public AccesoApi(String tokenInterno) {
    this.tokenInterno = tokenInterno;
  }

  public void verificar(Context ctx) {
    if (!ctx.matchedPath().startsWith("/api/")) return;
    ctx.header("Cache-Control", "no-store");

    if (esAvisoDeLogistica(ctx)) return;
    Usuario usuario = SesionUsuario.actual(ctx);
    if (usuario == null) {
      ctx.status(401).json(Map.of("error", "Iniciá sesión para acceder a la API."));
      ctx.skipRemainingHandlers();
    } else if (usuario.getRol() != Rol.ADMINISTRADORA) {
      ctx.status(403).json(Map.of("error", "Esta operación requiere una cuenta administradora."));
      ctx.skipRemainingHandlers();
    }
  }

  private boolean esAvisoDeLogistica(Context ctx) {
    if (!"PATCH".equals(ctx.req().getMethod())
        || !"/api/donaciones/{id}".equals(ctx.matchedPath())
        || tokenInterno == null || tokenInterno.isBlank()) return false;

    String recibido = ctx.header("X-Token-Interno");
    return recibido != null && MessageDigest.isEqual(
        tokenInterno.getBytes(StandardCharsets.UTF_8), recibido.getBytes(StandardCharsets.UTF_8));
  }
}
