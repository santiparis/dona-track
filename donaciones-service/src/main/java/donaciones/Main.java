package donaciones;

import donaciones.controller.AsignacionController;
import donaciones.controller.DonacionController;
import donaciones.controller.DonanteController;
import donaciones.controller.EntidadesBeneficiariasController;
import donaciones.controller.IntegracionLogisticaController;
import donaciones.controller.Notificador;
import donaciones.domain.donante.RepositorioPersonas;
import donaciones.repository.DonacionRepository;
import donaciones.repository.EntidadBeneficiariaRepository;
import donaciones.repository.PersonasAdministradorasRepository;
import donaciones.service.AsignacionService;
import donaciones.service.DonanteService;
import donaciones.retrofit_client.LogisticaAPICalls;
import donaciones.retrofit_client.RetrofitConfig;
import donaciones.service.EntidadBeneficiariaService;
import io.javalin.Javalin;

public class Main {
  public static void main(String[] args) {

    PersonasAdministradorasRepository administradorasRepo = new PersonasAdministradorasRepository();
    DonacionRepository donacionesRepository = new DonacionRepository();
    RepositorioPersonas personasRepository = new RepositorioPersonas();
    EntidadBeneficiariaRepository entidadRepo = new EntidadBeneficiariaRepository();

    RetrofitConfig retrofitConfig = new RetrofitConfig();
    LogisticaAPICalls logisticaAPICalls = retrofitConfig.logisticaAPICalls();

    Notificador notificador = new Notificador();

    DonacionController controller = new DonacionController(donacionesRepository, personasRepository, administradorasRepo, notificador);
    DonanteService donanteService = new DonanteService(personasRepository);
    DonanteController donanteController = new DonanteController(donanteService);
    IntegracionLogisticaController integracionLogisticaController = new IntegracionLogisticaController();

    AsignacionService asignacionService = new AsignacionService(donacionesRepository, entidadRepo, logisticaAPICalls, notificador);
    EntidadBeneficiariaService entidadService = new EntidadBeneficiariaService(entidadRepo);
    EntidadesBeneficiariasController entidadesController = new EntidadesBeneficiariasController(entidadService);

    AsignacionController asignacionController = new AsignacionController(asignacionService);


    Javalin app = Javalin.create().start(8081);

    app.get("/api/donaciones", controller::listar);
    app.post("/api/donaciones", controller::crear);
    app.put("/api/donaciones/{id}", controller::actualizar);
    app.patch("/api/donaciones/{id}", controller::actualizarParcial);
    app.patch("/api/donaciones/{id}/en-traslado", controller::marcarEnTraslado);
    app.patch("/api/donaciones/{id}/entregada", controller::confirmarEntrega);
    app.patch("/api/donaciones/{id}/entrega-fallida", controller::registrarEntregaFallida);
    app.delete("/api/donaciones/{id}", controller::eliminar);

    app.post("/donaciones/rutasIniciadas", integracionLogisticaController::rutasIniciadas);
    app.post("/donaciones/entregaCompletada", integracionLogisticaController::entregaCompletada);
    app.post("/donaciones/entregaFallida", integracionLogisticaController::entregaFallida);

    app.get("/api/donantes", donanteController::listar);
    app.post("/api/donantes", donanteController::crear);
    app.put("/api/donantes/{id}", donanteController::actualizar);
    app.delete("/api/donantes/{id}", donanteController::eliminar);

    app.get("/api/entidades-beneficiarias", entidadesController::getEntidadesBeneficiarias);
    app.post("/api/entidades-beneficiarias", entidadesController::postEntidadBeneficiaria);
    app.put("/api/entidades-beneficiarias/{id}", entidadesController::putEntidadBeneficiaria);
    app.patch("/api/entidades-beneficiarias/{id}", entidadesController::patchEntidadBeneficiaria);
    app.delete("/api/entidades-beneficiarias/{id}", entidadesController::deleteEntidadBeneficiaria);

    app.get("/api/entidades-beneficiarias/{id}/necesidades", entidadesController::getNecesidades);
    app.post("/api/entidades-beneficiarias/{id}/necesidades", entidadesController::postNecesidades);
    app.put("/api/entidades-beneficiarias/{id}/necesidades/{idNecesidad}", entidadesController::putNecesidad);
    app.patch("/api/entidades-beneficiarias/{id}/necesidades/{idNecesidad}", entidadesController::patchNecesidad);
    app.delete("/api/entidades-beneficiarias/{id}/necesidades/{idNecesidad}", entidadesController::deleteNecesidad);

    app.get("/api/donaciones/{id}/sugerencias", asignacionController::obtenerRanking);
    app.post("/api/donaciones/{id}/asignaciones/{idEntidad}", asignacionController::seleccionarEntidad);
  }
}
