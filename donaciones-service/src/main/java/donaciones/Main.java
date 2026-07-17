package donaciones;

import donaciones.controller.AsignacionController;
import donaciones.controller.DonacionController;
import donaciones.controller.DonanteController;
import donaciones.controller.EntidadesBeneficiariasController;
import donaciones.domain.donante.RepositorioPersonas;
import donaciones.domain.eventos.*;
import donaciones.domain.eventos.listeners.DonacionAsignadaListener;
import donaciones.domain.eventos.listeners.EntregaNoSatisfactoriaListener;
import donaciones.domain.eventos.listeners.EntregaRealizadaListener;
import donaciones.domain.eventos.listeners.InicioRutaListener;
import donaciones.repository.DonacionRepository;
import donaciones.repository.EntidadBeneficiariaRepository;
import donaciones.repository.PersonasAdministradorasRepository;
import donaciones.service.AsignacionService;
import donaciones.service.DonacionService;
import donaciones.service.DonanteService;
import donaciones.retrofit_client.LogisticaAPICalls;
import donaciones.retrofit_client.RetrofitConfig;
import donaciones.repository.DonanteRepository;
import donaciones.service.EntidadBeneficiariaService;
import io.javalin.Javalin;

public class Main {
  public static void main(String[] args) {
    // Publicador de eventos y listeners
    PersonasAdministradorasRepository adminRepo = new PersonasAdministradorasRepository();
    PublicadorDeEventos publicador = new PublicadorDeEventos();
    publicador.suscribir(DonacionAsignadaEvent.class, new DonacionAsignadaListener());
    publicador.suscribir(InicioRutaEvent.class, new InicioRutaListener());
    publicador.suscribir(EntregaRealizadaEvent.class, new EntregaRealizadaListener());
    publicador.suscribir(EntregaNoSatisfactoriaEvent.class, new EntregaNoSatisfactoriaListener(adminRepo));

    DonacionRepository donacionesRepository = new DonacionRepository();
    RepositorioPersonas personasRepository = new RepositorioPersonas();
    DonanteRepository donanteRepo = new DonanteRepository();
    EntidadBeneficiariaRepository entidadRepo = new EntidadBeneficiariaRepository();

    RetrofitConfig retrofitConfig = new RetrofitConfig();
    LogisticaAPICalls logisticaAPICalls = retrofitConfig.logisticaAPICalls();

    DonacionService service = new DonacionService(donacionesRepository, personasRepository, publicador);
    DonacionController controller = new DonacionController(service);
    DonanteService donanteService = new DonanteService(donanteRepo);
    DonanteController donanteController = new DonanteController(donanteService);

    AsignacionService asignacionService = new AsignacionService(donacionesRepository, entidadRepo, logisticaAPICalls, publicador);
    EntidadBeneficiariaService entidadService = new EntidadBeneficiariaService(entidadRepo);
    EntidadesBeneficiariasController entidadesController = new EntidadesBeneficiariasController(entidadService);

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
    app.delete("/api/entidades-beneficiarias/{id}/necesidades/{idNecesidad}", entidadesController::deleteNecesidad);

    app.get("/api/donaciones/{id}/sugerencias", asignacionController::obtenerRanking);
    app.post("/api/donaciones/{id}/asignaciones/{idEntidad}", asignacionController::seleccionarEntidad);
  }
}
