package donaciones.web.templates;

import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.helper.ConditionalHelpers;
import com.github.jknack.handlebars.io.ClassPathTemplateLoader;
import io.javalin.http.Context;
import io.javalin.rendering.FileRenderer;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Map;

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
      return handlebars.compile(filePath).apply(model);
    } catch (IOException e) {
      throw new UncheckedIOException("No se pudo renderizar la plantilla " + filePath, e);
    }
  }
}
