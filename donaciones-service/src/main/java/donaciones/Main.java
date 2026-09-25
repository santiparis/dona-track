package donaciones;

import donaciones.controller.*;
import donaciones.domain.algoritmos.CompatibilidadSemantica;
import donaciones.domain.algoritmos.OrganizadorAsignaciones;
import donaciones.domain.algoritmos.PrioridadSubAtendidos;
import donaciones.domain.donante.RepositorioPersonas;
import donaciones.repository.DonacionRepository;
import donaciones.repository.EntidadBeneficiariaRepository;
import donaciones.repository.PersonasAdministradorasRepository;
import donaciones.repository.NotificacionRepository;
import donaciones.repository.SugerenciaAsignacionRepository;
import donaciones.retrofit_client.LogisticaAPICalls;
import donaciones.retrofit_client.RetrofitConfig;
import io.javalin.Javalin;
import donaciones.controller.*;

import java.util.List;

public class Main {
  public static void main(String[] args) {

    PersonasAdministradorasRepository administradorasRepo = new PersonasAdministradorasRepository();
    DonacionRepository donacionesRepository = new DonacionRepository();
    RepositorioPersonas personasRepository = new RepositorioPersonas();
    EntidadBeneficiariaRepository entidadRepo = new EntidadBeneficiariaRepository();

    RetrofitConfig retrofitConfig = new RetrofitConfig();
    LogisticaAPICalls logisticaAPICalls = retrofitConfig.logisticaAPICalls();

    Notificador notificador = new Notificador(new NotificacionRepository());

    DonacionController controller = new DonacionController(donacionesRepository, personasRepository, administradorasRepo, notificador);
    DonanteController donanteController = new DonanteController(personasRepository);
    IntegracionLogisticaController integracionLogisticaController = new IntegracionLogisticaController();
    SugerenciaAsignacionRepository sugerenciasRepository = new SugerenciaAsignacionRepository();

    EntidadesBeneficiariasController entidadesController = new EntidadesBeneficiariasController(entidadRepo);

    OrganizadorAsignaciones organizadorAsignaciones = new OrganizadorAsignaciones(List.of(new CompatibilidadSemantica(), new PrioridadSubAtendidos()));

    AsignacionesController asignacionesController = new AsignacionesController(
        donacionesRepository, entidadRepo, sugerenciasRepository, logisticaAPICalls, notificador, organizadorAsignaciones);


    Javalin app = Javalin.create().start(8081);

    // CRUD Donaciones
    app.get("/api/donaciones", controller::listar);
    app.get("/api/donaciones/{id}", controller::obtener);
    app.post("/api/donaciones", controller::crear);
    app.put("/api/donaciones/{id}", controller::actualizar);
    app.patch("/api/donaciones/{id}", controller::actualizarParcial);
    app.delete("/api/donaciones/{id}", controller::eliminar);

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
