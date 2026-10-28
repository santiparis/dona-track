package donaciones.web;

import donaciones.web.controllers.DonanteController;
import donaciones.web.controllers.PublicoController;
import donaciones.web.controllers.SesionController;
import io.javalin.Javalin;

public class Router {

  public void configure(Javalin app) {
    PublicoController publicoController = new PublicoController();
    SesionController sesionController = new SesionController();
    DonanteController donanteController = new DonanteController();

    app.get("/", ctx -> ctx.render("publico/inicio", publicoController.inicio()));
    app.get("/legal", ctx -> ctx.render("publico/legal", publicoController.legal()));
    app.get("/donaciones-entregadas", ctx -> ctx.render("publico/entregadas", publicoController.entregadas()));

    app.get("/login", ctx -> ctx.render("sesion/login", sesionController.login()));
    app.post("/login", sesionController::ingresar);
    app.get("/usuarios/nuevo", ctx -> ctx.render("sesion/registro", sesionController.registro(ctx)));
    app.post("/usuarios", sesionController::registrar);

    app.get("/donante/donaciones", ctx -> ctx.render("donante/donaciones", donanteController.donaciones(ctx)));
    app.get("/donante/entidades", ctx -> ctx.render("donante/entidades", donanteController.entidades(ctx)));
    app.get("/donante/incentivos", ctx -> ctx.render("donante/incentivos", donanteController.incentivos()));
  }
}
