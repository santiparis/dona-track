package donaciones.web;

import donaciones.controller.AsignacionesController;
import donaciones.controller.CamionesController;
import donaciones.controller.DonacionController;
import donaciones.controller.DonanteController;
import donaciones.controller.EntidadesBeneficiariasController;
import donaciones.controller.NotificacionesController;
import donaciones.controller.PublicoController;
import donaciones.controller.SesionController;
import io.javalin.Javalin;

public class Router {

  private final DonacionController donacionController;
  private final DonanteController donanteController;
  private final EntidadesBeneficiariasController entidadesController;
  private final AsignacionesController asignacionesController;
  private final CamionesController camionesController;
  private final NotificacionesController notificacionesController;
  private final PublicoController publicoController;
  private final SesionController sesionController;

  public Router(DonacionController donacionController, DonanteController donanteController,
                EntidadesBeneficiariasController entidadesController, AsignacionesController asignacionesController,
                CamionesController camionesController, NotificacionesController notificacionesController,
                PublicoController publicoController, SesionController sesionController) {
    this.donacionController = donacionController;
    this.donanteController = donanteController;
    this.entidadesController = entidadesController;
    this.asignacionesController = asignacionesController;
    this.camionesController = camionesController;
    this.notificacionesController = notificacionesController;
    this.publicoController = publicoController;
    this.sesionController = sesionController;
  }

  public void configure(Javalin app) {

    app.get("/", ctx -> ctx.render("publico/inicio", publicoController.inicio()));
    app.get("/legal", ctx -> ctx.render("publico/legal", publicoController.legal()));
    app.get("/donaciones-entregadas", ctx -> ctx.render("publico/entregadas", donacionController.entregadas()));

    app.get("/login", ctx -> ctx.render("sesion/login", sesionController.login()));
    app.post("/login", sesionController::ingresar);
    app.get("/usuarios/nuevo", ctx -> ctx.render("sesion/registro", sesionController.registro(ctx)));
    app.post("/usuarios", sesionController::registrar);

    app.get("/donante/donaciones", ctx -> ctx.render("donante/donaciones", donacionController.misDonaciones(ctx)));
    app.get("/donante/entidades", ctx -> ctx.render("donante/entidades", entidadesController.entidades(ctx)));
    app.get("/donante/incentivos", ctx -> ctx.render("donante/incentivos", donanteController.incentivos()));
    app.get("/donante/notificaciones", ctx -> ctx.render("notificaciones", notificacionesController.notificacionesDonante()));
    app.get("/donante/seguimiento", ctx -> ctx.render("seguimiento", camionesController.seguimientoDonante()));

    app.get("/entidad/necesidades", ctx -> ctx.render("entidad/necesidades", entidadesController.necesidades(ctx)));
    app.post("/entidad/necesidades", entidadesController::registrarNecesidad);
    app.post("/entidad/necesidades/{id}", entidadesController::modificarNecesidad);
    app.post("/entidad/necesidades/{id}/eliminar", entidadesController::eliminarNecesidad);
    app.get("/entidad/donaciones", ctx -> ctx.render("entidad/donaciones", donacionController.asignadasAEntidad(ctx)));
    app.get("/entidad/donaciones/{id}/recepcion", ctx -> ctx.render("entidad/recepcion", donacionController.recepcion(ctx)));
    app.post("/entidad/donaciones/{id}/recepcion", donacionController::confirmarRecepcion);
    app.post("/entidad/donaciones/{id}/no-recibida", donacionController::informarNoRecibida);
    app.get("/entidad/notificaciones", ctx -> ctx.render("notificaciones", notificacionesController.notificacionesEntidad()));
    app.get("/entidad/seguimiento", ctx -> ctx.render("seguimiento", camionesController.seguimientoEntidad()));

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
    app.get("/admin/camiones", ctx -> ctx.render("admin/camiones", camionesController.camiones(ctx)));
    app.post("/admin/camiones", camionesController::crearCamion);
    app.post("/admin/camiones/{id}", camionesController::modificarCamion);
    app.post("/admin/camiones/{id}/eliminar", camionesController::eliminarCamion);
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
