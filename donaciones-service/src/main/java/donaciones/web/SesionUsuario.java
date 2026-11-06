package donaciones.web;

import donaciones.domain.donante.Usuario;
import donaciones.domain.donante.Rol;
import donaciones.repository.UsuarioRepository;
import io.javalin.config.JavalinConfig;
import io.javalin.http.Context;
import io.javalin.http.ForbiddenResponse;
import io.javalin.http.HttpStatus;
import org.eclipse.jetty.http.HttpCookie;
import jakarta.servlet.SessionTrackingMode;
import java.util.Set;

public final class SesionUsuario {
    public static final String USUARIO_ID = "usuarioId";
    public static final String ROL = "rol";
    private static final String USUARIO_ACTUAL = "usuarioActual";

    private SesionUsuario() { }

    public static void configurar(JavalinConfig config) {
        config.jetty.modifyServletContextHandler(handler -> {
            var sesiones = handler.getSessionHandler();
            sesiones.setHttpOnly(true);
            sesiones.setSameSite(HttpCookie.SameSite.LAX);
            sesiones.setSecureRequestOnly(true);
            sesiones.setMaxInactiveInterval(30 * 60);
            // El identificador viaja solo en la cookie, nunca en enlaces de la página.
            sesiones.setSessionTrackingModes(Set.of(SessionTrackingMode.COOKIE));
        });
    }

    public static void iniciar(Context ctx, Usuario usuario) {
        // Una sesión nueva evita reutilizar un identificador previo al login.
        var anterior = ctx.req().getSession(false);
        if (anterior != null) anterior.invalidate();
        ctx.sessionAttribute(USUARIO_ID, usuario.getId());
        ctx.sessionAttribute(ROL, usuario.getRol());
        ctx.attribute(USUARIO_ACTUAL, usuario);
    }

    public static Usuario actual(Context ctx) {
        Usuario usuario = ctx.attribute(USUARIO_ACTUAL);
        if (usuario != null) return usuario;
        var sesion = ctx.req().getSession(false);
        if (sesion == null || !(sesion.getAttribute(USUARIO_ID) instanceof Long id)) return null;

        // Se consulta una vez por petición: si borraron la cuenta o cambiaron su rol,
        // los permisos no quedan congelados hasta que venza la sesión.
        usuario = new UsuarioRepository().buscarPorId(id).orElse(null);
        if (usuario == null) {
            cerrar(ctx);
        } else {
            ctx.attribute(USUARIO_ACTUAL, usuario);
        }
        return usuario;
    }

    public static void verificarAcceso(Context ctx) {
        String ruta = ctx.matchedPath();
        Rol requerido = null;
        if (ruta.startsWith("/admin/")) requerido = Rol.ADMINISTRADORA;
        if (ruta.startsWith("/donante/")) requerido = Rol.DONANTE;
        if (ruta.startsWith("/entidad/")) requerido = Rol.ENTIDAD;
        if (requerido == null) return;

        ctx.header("Cache-Control", "no-store");
        Usuario usuario = actual(ctx);
        if (usuario == null) {
            ctx.redirect("/login", HttpStatus.SEE_OTHER);
            ctx.skipRemainingHandlers();
        } else if (usuario.getRol() != requerido) {
            throw new ForbiddenResponse("Tu cuenta no tiene acceso a esta sección.");
        }
    }

    public static void cerrar(Context ctx) {
        var sesion = ctx.req().getSession(false);
        if (sesion != null) sesion.invalidate();
        ctx.attribute(USUARIO_ACTUAL, null);
        ctx.removeCookie("JSESSIONID", "/");
    }

    public static String destino(Rol rol) {
        return switch (rol) {
            case DONANTE -> "/donante/donaciones";
            case ENTIDAD -> "/entidad/necesidades";
            case ADMINISTRADORA -> "/admin/donaciones";
        };
    }
}
