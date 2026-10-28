package donaciones.web;

import donaciones.web.controllers.AdminController;
import donaciones.web.controllers.DonanteController;
import donaciones.web.controllers.EntidadController;
import donaciones.web.controllers.PublicoController;
import donaciones.web.controllers.SesionController;
import io.javalin.Javalin;

public class Router {

  public void configure(Javalin app) {
    PublicoController publicoController = new PublicoController();
    SesionController sesionController = new SesionController();
    DonanteController donanteController = new DonanteController();
    EntidadController entidadController = new EntidadController();
    AdminController adminController = new AdminController();

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
    app.get("/donante/notificaciones", ctx -> ctx.render("notificaciones", donanteController.notificaciones()));
    app.get("/donante/seguimiento", ctx -> ctx.render("seguimiento", donanteController.seguimiento()));

    app.get("/entidad/necesidades", ctx -> ctx.render("entidad/necesidades", entidadController.necesidades(ctx)));
    app.post("/entidad/necesidades", entidadController::crearNecesidad);
    app.post("/entidad/necesidades/{id}", entidadController::modificarNecesidad);
    app.post("/entidad/necesidades/{id}/eliminar", entidadController::eliminarNecesidad);
    app.get("/entidad/donaciones", ctx -> ctx.render("entidad/donaciones", entidadController.donaciones(ctx)));
    app.get("/entidad/donaciones/{id}/recepcion", ctx -> ctx.render("entidad/recepcion", entidadController.recepcion(ctx)));
    app.post("/entidad/donaciones/{id}/recepcion", entidadController::confirmarRecepcion);
    app.post("/entidad/donaciones/{id}/no-recibida", entidadController::informarNoRecibida);
    app.get("/entidad/notificaciones", ctx -> ctx.render("notificaciones", entidadController.notificaciones()));
    app.get("/entidad/seguimiento", ctx -> ctx.render("seguimiento", entidadController.seguimiento()));

    app.get("/admin/donantes/nuevo", ctx -> ctx.render("admin/donante-nuevo", adminController.donanteNuevo(ctx)));
    app.post("/admin/donantes", adminController::registrarDonante);
    app.get("/admin/donaciones/nueva", ctx -> ctx.render("admin/donacion-nueva", adminController.donacionNueva(ctx)));
    app.post("/admin/donaciones", adminController::registrarDonacion);
    app.get("/admin/donaciones", ctx -> ctx.render("admin/deposito", adminController.deposito(ctx)));
    app.post("/admin/donaciones/{id}/vencida", adminController::marcarVencida);
  }
}
