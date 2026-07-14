package donaciones;

import donaciones.controller.AsignacionController;
import donaciones.controller.DonacionController;
import donaciones.controller.DonanteController;
import donaciones.domain.donante.RepositorioPersonas;
import donaciones.repository.DonacionRepository;
import donaciones.repository.EntidadBeneficiariaRepository;
import donaciones.service.AsignacionService;
import donaciones.service.DonacionService;
import donaciones.service.DonanteService;
import donaciones.retrofit_client.LogisticaAPICalls;
import donaciones.retrofit_client.RetrofitConfig;
import donaciones.repository.DonanteRepository;
import io.javalin.Javalin;

public class Main {
  public static void main(String[] args) {

    DonacionRepository donacionesRepository = new DonacionRepository();
    RepositorioPersonas personasRepository = new RepositorioPersonas();
    DonanteRepository donanteRepo = new DonanteRepository();
    EntidadBeneficiariaRepository entidadRepo = new EntidadBeneficiariaRepository();

    RetrofitConfig retrofitConfig = new RetrofitConfig();
    LogisticaAPICalls logisticaAPICalls = retrofitConfig.logisticaAPICalls();

    DonacionService service = new DonacionService(donacionesRepository, personasRepository);
    DonacionController controller = new DonacionController(service);
    DonanteService donanteService = new DonanteService(donanteRepo);
    DonanteController donanteController = new DonanteController(donanteService);

    AsignacionService asignacionService = new AsignacionService(donacionesRepository, entidadRepo, logisticaAPICalls);
    AsignacionController asignacionController = new AsignacionController(asignacionService);

    Javalin app = Javalin.create().start(8081);

    app.get("/api/donaciones", controller::listar);
    app.post("/api/donaciones", controller::crear);
    app.patch("/api/donaciones/{id}/estado", controller::cambiarEstado);
    app.delete("/api/donaciones/{id}", controller::eliminar);

    app.get("/api/donantes", donanteController::listar);
    app.post("/api/donantes", donanteController::crear);
    app.put("/api/donantes/{documento}", donanteController::actualizar);
    app.delete("/api/donantes/{documento}", donanteController::eliminar);

    app.get("/api/donaciones/{id}/sugerencias", asignacionController::obtenerRanking);
    app.post("/api/donaciones/{id}/asignar", asignacionController::seleccionarEntidad);
  }
}
