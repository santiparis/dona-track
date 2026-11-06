package donaciones.web.templates;

import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.helper.ConditionalHelpers;
import com.github.jknack.handlebars.io.ClassPathTemplateLoader;
import io.javalin.http.Context;
import io.javalin.rendering.FileRenderer;
import donaciones.web.SesionUsuario;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Map;
import java.util.HashMap;

public class JavalinHandlebars implements FileRenderer {

  private final Handlebars handlebars;

  public JavalinHandlebars() {
    // Los .hbs se leen de src/main/resources/templates (quedan en el classpath al compilar)
    this.handlebars = new Handlebars(new ClassPathTemplateLoader("/templates", ".hbs"));
    // eq, neq, etc. vienen con Handlebars pero no se registran solos
    this.handlebars.registerHelpers(ConditionalHelpers.class);
  }

  @NotNull
  @Override
  public String render(@NotNull String filePath, @NotNull Map<String, ?> model, @NotNull Context context) {
    try {
      // Datos comunes del encabezado, siempre tomados de la cuenta autenticada.
      Map<String, Object> vista = new HashMap<>(model);
      vista.remove("usuario");
      var usuario = SesionUsuario.actual(context);
      if (usuario != null) {
        vista.put("usuario", Map.of("nombre", usuario.getNombreUsuario()));
        vista.put("inicioUsuario", SesionUsuario.destino(usuario.getRol()));
        context.header("Cache-Control", "no-store");
      }
      return handlebars.compile(filePath).apply(vista);
    } catch (IOException e) {
      throw new UncheckedIOException("No se pudo renderizar la plantilla " + filePath, e);
    }
  }
}
