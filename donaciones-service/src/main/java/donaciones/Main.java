package donaciones;

import donaciones.controller.AsignacionController;
import donaciones.controller.DonacionController;
import donaciones.controller.DonanteController;
import donaciones.controller.EntidadesBeneficiariasController;
import donaciones.domain.donante.RepositorioPersonas;
import donaciones.repository.DonacionRepository;
import donaciones.repository.EntidadBeneficiariaRepository;
import donaciones.service.AsignacionService;
import donaciones.service.DonacionService;
import donaciones.service.DonanteService;
import donaciones.service.EntidadBeneficiariaService;
import io.javalin.Javalin;

public class Main {
  public static void main(String[] args) {
    DonacionRepository donacionesRepository = new DonacionRepository();
    RepositorioPersonas personasRepository = new RepositorioPersonas();
    DonacionService service = new DonacionService(donacionesRepository, personasRepository);
    DonacionController controller = new DonacionController(service);

    donaciones.repository.DonanteRepository donanteRepo = new donaciones.repository.DonanteRepository();
    DonanteService donanteService = new DonanteService(donanteRepo);
    DonanteController donanteController = new DonanteController(donanteService);

    EntidadBeneficiariaRepository entidadRepo = new EntidadBeneficiariaRepository();
    EntidadBeneficiariaService entidadService = new EntidadBeneficiariaService(entidadRepo);
    EntidadesBeneficiariasController entidadesController = new EntidadesBeneficiariasController(entidadService);
    AsignacionService asignacionService = new AsignacionService(donacionesRepository, entidadRepo);
    AsignacionController asignacionController = new AsignacionController(asignacionService);

    Javalin app = Javalin.create().start(8081);

    app.get("/api/donaciones", controller::listar);
    app.post("/api/donaciones", controller::crear);
    app.put("/api/donaciones/{id}", controller::actualizar);
    app.patch("/api/donaciones/{id}", controller::actualizarParcial);
    app.patch("/api/donaciones/{id}/estado", controller::cambiarEstado);
    app.delete("/api/donaciones/{id}", controller::eliminar);

    app.get("/api/donantes", donanteController::listar);
    app.post("/api/donantes", donanteController::crear);
    app.put("/api/donantes/{documento}", donanteController::actualizar);
    app.delete("/api/donantes/{documento}", donanteController::eliminar);

    app.get("/api/entidades-beneficiarias", entidadesController::getEntidadesBeneficiarias);
    app.post("/api/entidades-beneficiarias", entidadesController::postEntidadBeneficiaria);
    app.put("/api/entidades-beneficiarias/{id}", entidadesController::putEntidadBeneficiaria);
    app.patch("/api/entidades-beneficiarias/{id}", entidadesController::patchEntidadBeneficiaria);
    app.delete("/api/entidades-beneficiarias/{id}", entidadesController::deleteEntidadBeneficiaria);

    app.get("/api/entidades-beneficiarias/{id}/necesidades", entidadesController::getNecesidades);
    app.post("/api/entidades-beneficiarias/{id}/necesidades", entidadesController::postNecesidades);
    app.put("/api/entidades-beneficiarias/{id}/necesidades/{idNecesidad}", entidadesController::putNecesidad);
    app.patch("/api/entidades-beneficiarias/{id}/necesidades/{idNecesidad}", entidadesController::patchNecesidad);
    app.delete("/api/entidades-beneficiarias/{idEntidad}/necesidades/{idNecesidad}", entidadesController::deleteNecesidad);

    app.get("/api/donaciones/{id}/sugerencias", asignacionController::obtenerRanking);
    app.post("/api/donaciones/{id}/asignar", asignacionController::seleccionarEntidad);
  }
}
