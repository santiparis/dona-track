package donaciones;

import donaciones.controller.AsignacionController;
import donaciones.controller.DonacionController;
import donaciones.controller.DonanteController;
import donaciones.repository.DonacionRepository;
import donaciones.repository.EntidadBeneficiariaRepository;
import donaciones.service.AsignacionService;
import donaciones.service.DonacionService;
import donaciones.service.DonanteService;
import io.javalin.Javalin;

public class Main {
  public static void main(String[] args) {
    DonacionRepository repository = new DonacionRepository();
    DonacionService service = new DonacionService(repository);
    DonacionController controller = new DonacionController(service);

    donaciones.repository.DonanteRepository donanteRepo = new donaciones.repository.DonanteRepository();
    DonanteService donanteService = new DonanteService(donanteRepo);
    DonanteController donanteController = new DonanteController(donanteService);

    EntidadBeneficiariaRepository entidadRepo = new EntidadBeneficiariaRepository();
    AsignacionService asignacionService = new AsignacionService(repository, entidadRepo);
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
    app.post("/api/donaciones/{id}/asignar/{idEntidad}", asignacionController::seleccionarEntidad);
  }
}
