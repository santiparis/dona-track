package donaciones.web;

import donaciones.web.controllers.PublicoController;
import io.javalin.Javalin;

public class Router {

  public void configure(Javalin app) {
    PublicoController publicoController = new PublicoController();

    app.get("/", ctx -> ctx.render("publico/inicio", publicoController.inicio()));
  }
}
