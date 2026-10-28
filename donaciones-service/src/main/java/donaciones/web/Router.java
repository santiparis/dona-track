package donaciones.web;

import donaciones.controller.AsignacionesController;
import donaciones.controller.DonacionController;
import donaciones.controller.DonanteController;
import donaciones.controller.EntidadesBeneficiariasController;
import donaciones.web.controllers.AdminController;
import donaciones.web.controllers.EntidadController;
import donaciones.web.controllers.PublicoController;
import donaciones.web.controllers.SesionController;
import io.javalin.Javalin;

public class Router {

  private final DonacionController donacionController;
  private final DonanteController donanteController;
  private final EntidadesBeneficiariasController entidadesController;
  private final AsignacionesController asignacionesController;

  public Router(DonacionController donacionController, DonanteController donanteController,
                EntidadesBeneficiariasController entidadesController, AsignacionesController asignacionesController) {
    this.donacionController = donacionController;
    this.donanteController = donanteController;
    this.entidadesController = entidadesController;
    this.asignacionesController = asignacionesController;
  }

  public void configure(Javalin app) {
    PublicoController publicoController = new PublicoController();
    SesionController sesionController = new SesionController();
    // Hasta mover sus métodos conviven dos DonanteController: el de la API (campo) y este, por perfil
    donaciones.web.controllers.DonanteController perfilDonanteController = new donaciones.web.controllers.DonanteController();
    EntidadController entidadController = new EntidadController();
    AdminController adminController = new AdminController();

    app.get("/", ctx -> ctx.render("publico/inicio", publicoController.inicio()));
    app.get("/legal", ctx -> ctx.render("publico/legal", publicoController.legal()));
    app.get("/donaciones-entregadas", ctx -> ctx.render("publico/entregadas", donacionController.entregadas()));

    app.get("/login", ctx -> ctx.render("sesion/login", sesionController.login()));
    app.post("/login", sesionController::ingresar);
    app.get("/usuarios/nuevo", ctx -> ctx.render("sesion/registro", sesionController.registro(ctx)));
    app.post("/usuarios", sesionController::registrar);

    app.get("/donante/donaciones", ctx -> ctx.render("donante/donaciones", donacionController.misDonaciones(ctx)));
    app.get("/donante/entidades", ctx -> ctx.render("donante/entidades", perfilDonanteController.entidades(ctx)));
    app.get("/donante/incentivos", ctx -> ctx.render("donante/incentivos", donanteController.incentivos()));
    app.get("/donante/notificaciones", ctx -> ctx.render("notificaciones", perfilDonanteController.notificaciones()));
    app.get("/donante/seguimiento", ctx -> ctx.render("seguimiento", perfilDonanteController.seguimiento()));

    app.get("/entidad/necesidades", ctx -> ctx.render("entidad/necesidades", entidadController.necesidades(ctx)));
    app.post("/entidad/necesidades", entidadController::crearNecesidad);
    app.post("/entidad/necesidades/{id}", entidadController::modificarNecesidad);
    app.post("/entidad/necesidades/{id}/eliminar", entidadController::eliminarNecesidad);
    app.get("/entidad/donaciones", ctx -> ctx.render("entidad/donaciones", donacionController.asignadasAEntidad(ctx)));
    app.get("/entidad/donaciones/{id}/recepcion", ctx -> ctx.render("entidad/recepcion", donacionController.recepcion(ctx)));
    app.post("/entidad/donaciones/{id}/recepcion", donacionController::confirmarRecepcion);
    app.post("/entidad/donaciones/{id}/no-recibida", donacionController::informarNoRecibida);
    app.get("/entidad/notificaciones", ctx -> ctx.render("notificaciones", entidadController.notificaciones()));
    app.get("/entidad/seguimiento", ctx -> ctx.render("seguimiento", entidadController.seguimiento()));

    app.get("/admin/donantes/nuevo", ctx -> ctx.render("admin/donante-nuevo", donanteController.donanteNuevo(ctx)));
    app.post("/admin/donantes", donanteController::registrarDonante);
    app.get("/admin/donaciones/nueva", ctx -> ctx.render("admin/donacion-nueva", donacionController.donacionNueva(ctx)));
    app.post("/admin/donaciones", donacionController::registrarDonacion);
    app.get("/admin/donaciones", ctx -> ctx.render("admin/deposito", donacionController.deposito(ctx)));
    app.post("/admin/donaciones/{id}/vencida", donacionController::marcarVencida);
    app.get("/admin/asignaciones", ctx -> ctx.render("admin/asignaciones", asignacionesController.asignaciones(ctx)));
    app.post("/admin/asignaciones/ejecutar", asignacionesController::ejecutarAlgoritmosAhora);
    app.get("/admin/asignaciones/{id}", ctx -> ctx.render("admin/asignacion", asignacionesController.asignacion(ctx)));
    app.post("/admin/asignaciones/{id}", asignacionesController::confirmarDestino);
    app.get("/admin/camiones", ctx -> ctx.render("admin/camiones", adminController.camiones(ctx)));
    app.post("/admin/camiones", adminController::crearCamion);
    app.post("/admin/camiones/{id}", adminController::modificarCamion);
    app.post("/admin/camiones/{id}/eliminar", adminController::eliminarCamion);
    app.get("/admin/importacion", ctx -> ctx.render("admin/importacion", donanteController.importacion(ctx)));
    app.post("/admin/importacion", donanteController::importar);

    // API REST

    // CRUD Donaciones
    app.get("/api/donaciones", donacionController::listar);
    app.get("/api/donaciones/{id}", donacionController::obtener);
    app.post("/api/donaciones", donacionController::crear);
    app.put("/api/donaciones/{id}", donacionController::actualizar);
    app.patch("/api/donaciones/{id}", donacionController::actualizarParcial);
    app.delete("/api/donaciones/{id}", donacionController::eliminar);

    // CRUD Donantes
    app.get("/api/donantes", donanteController::listar);
    app.get("/api/donantes/{id}", donanteController::obtener);
    app.post("/api/donantes", donanteController::crear);
    app.put("/api/donantes/{id}", donanteController::actualizar);
    app.patch("/api/donantes/{id}", donanteController::actualizarParcial);
    app.delete("/api/donantes/{id}", donanteController::eliminar);

    // CRUD Entidades beneficiarias
    app.get("/api/entidades-beneficiarias", entidadesController::getEntidadesBeneficiarias);
    app.get("/api/entidades-beneficiarias/{id}", entidadesController::getEntidadBeneficiaria);
    app.post("/api/entidades-beneficiarias", entidadesController::postEntidadBeneficiaria);
    app.put("/api/entidades-beneficiarias/{id}", entidadesController::putEntidadBeneficiaria);
    app.patch("/api/entidades-beneficiarias/{id}", entidadesController::patchEntidadBeneficiaria);
    app.delete("/api/entidades-beneficiarias/{id}", entidadesController::deleteEntidadBeneficiaria);

    // CRUD Necesidades
    app.get("/api/entidades-beneficiarias/{id}/necesidades", entidadesController::getNecesidades);
    app.post("/api/entidades-beneficiarias/{id}/necesidades", entidadesController::postNecesidades);
    app.put("/api/entidades-beneficiarias/{id}/necesidades/{idNecesidad}", entidadesController::putNecesidad);
    app.patch("/api/entidades-beneficiarias/{id}/necesidades/{idNecesidad}", entidadesController::patchNecesidad);
    app.delete("/api/entidades-beneficiarias/{id}/necesidades/{idNecesidad}", entidadesController::deleteNecesidad);

    app.get("/api/sugerencias", asignacionesController::getSugerencias);
    app.post("/api/sugerencias", asignacionesController::ejecutarAlgoritmos);
    app.get("/api/sugerencias/{id}/coincidencias", asignacionesController::getCoincidencias);
    app.get("/api/sugerencias/{id}/algoritmos", asignacionesController::getEntidadesPorAlgoritmo);
    app.patch("/api/sugerencias/{id}/asignaciones", asignacionesController::asignarDonacion);
  }
}
