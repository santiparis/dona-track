package donaciones.web;

import donaciones.domain.donante.Usuario;
import io.javalin.config.JavalinConfig;
import io.javalin.http.Context;
import org.eclipse.jetty.http.HttpCookie;

public final class SesionUsuario {
    public static final String USUARIO_ID = "usuarioId";
    public static final String ROL = "rol";

    private SesionUsuario() { }

    public static void configurar(JavalinConfig config) {
        config.jetty.modifyServletContextHandler(handler -> {
            var sesiones = handler.getSessionHandler();
            sesiones.setHttpOnly(true);
            sesiones.setSameSite(HttpCookie.SameSite.LAX);
            sesiones.setSecureRequestOnly(true);
            sesiones.setMaxInactiveInterval(30 * 60);
        });
    }

    public static void iniciar(Context ctx, Usuario usuario) {
        // Una sesión nueva evita reutilizar un identificador previo al login.
        var anterior = ctx.req().getSession(false);
        if (anterior != null) anterior.invalidate();
        ctx.sessionAttribute(USUARIO_ID, usuario.getId());
        ctx.sessionAttribute(ROL, usuario.getRol());
    }
}
