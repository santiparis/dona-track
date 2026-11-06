package sesion;

import donaciones.domain.donante.Rol;
import donaciones.domain.donante.Usuario;
import donaciones.web.AccesoApi;
import io.javalin.http.Context;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.mockito.Mockito.*;

class AccesoApiTest {
  private static final String TOKEN = "token-interno-de-prueba";

  @Test
  void rechazaAnonimosConJsonSinEjecutarLaOperacion() {
    Context ctx = contexto("GET", "/api/donantes", null, null);
    new AccesoApi(TOKEN).verificar(ctx);
    verify(ctx).status(401);
    verify(ctx).json(Map.of("error", "Iniciá sesión para acceder a la API."));
    verify(ctx).skipRemainingHandlers();
  }

  @Test
  void permiteAdministracionYRechazaOtrosRoles() {
    for (Rol rol : Rol.values()) {
      Context ctx = contexto("DELETE", "/api/donaciones/{id}", rol, null);
      new AccesoApi(TOKEN).verificar(ctx);
      if (rol == Rol.ADMINISTRADORA) {
        verify(ctx, never()).skipRemainingHandlers();
      } else {
        verify(ctx).status(403);
        verify(ctx).skipRemainingHandlers();
      }
    }
  }

  @Test
  void permiteElAvisoDeLogisticaConTokenCorrecto() {
    Context ctx = contexto("PATCH", "/api/donaciones/{id}", null, TOKEN);
    new AccesoApi(TOKEN).verificar(ctx);
    verify(ctx, never()).skipRemainingHandlers();
    verify(ctx, never()).attribute("usuarioActual");
  }

  @Test
  void elTokenNoAutorizaOtrosMetodosNiRutas() {
    for (String metodo : new String[]{"GET", "POST", "PUT", "DELETE"}) {
      Context ctx = contexto(metodo, "/api/donaciones/{id}", null, TOKEN);
      new AccesoApi(TOKEN).verificar(ctx);
      verify(ctx).status(401);
      verify(ctx).skipRemainingHandlers();
    }
    Context otraRuta = contexto("PATCH", "/api/donantes/{id}", null, TOKEN);
    new AccesoApi(TOKEN).verificar(otraRuta);
    verify(otraRuta).status(401);
    verify(otraRuta).skipRemainingHandlers();
  }

  @Test
  void sinTokenConfiguradoOConTokenIncorrectoNoHayExcepcion() {
    for (String configurado : new String[]{null, "", "  ", "otro-token"}) {
      Context ctx = contexto("PATCH", "/api/donaciones/{id}", null, TOKEN);
      new AccesoApi(configurado).verificar(ctx);
      verify(ctx).status(401);
      verify(ctx).skipRemainingHandlers();
    }
    Context sinHeader = contexto("PATCH", "/api/donaciones/{id}", null, null);
    new AccesoApi(TOKEN).verificar(sinHeader);
    verify(sinHeader).status(401);
  }

  @Test
  void noIntervieneFueraDeLaApi() {
    Context ctx = contexto("GET", "/login", null, null);
    new AccesoApi(TOKEN).verificar(ctx);
    verify(ctx, never()).skipRemainingHandlers();
    verify(ctx, never()).attribute("usuarioActual");
  }

  private Context contexto(String metodo, String ruta, Rol rol, String token) {
    Context ctx = mock(Context.class, RETURNS_SELF);
    HttpServletRequest request = mock(HttpServletRequest.class);
    when(ctx.req()).thenReturn(request);
    when(request.getMethod()).thenReturn(metodo);
    when(ctx.matchedPath()).thenReturn(ruta);
    when(ctx.header("X-Token-Interno")).thenReturn(token);
    if (rol != null) {
      Usuario usuario = mock(Usuario.class);
      when(usuario.getRol()).thenReturn(rol);
      when(ctx.attribute("usuarioActual")).thenReturn(usuario);
    }
    return ctx;
  }
}
