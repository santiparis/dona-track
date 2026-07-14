package donaciones;

import donaciones.controller.AsignacionController;
import donaciones.controller.DonacionController;
import donaciones.controller.DonanteController;
import donaciones.domain.donante.RepositorioPersonas;
import donaciones.domain.eventos.*;
import donaciones.domain.eventos.listeners.DonacionAsignadaListener;
import donaciones.domain.eventos.listeners.EntregaNoSatisfactoriaListener;
import donaciones.domain.eventos.listeners.EntregaRealizadaListener;
import donaciones.domain.eventos.listeners.InicioRutaListener;
import donaciones.repository.DonacionRepository;
import donaciones.repository.EntidadBeneficiariaRepository;
import donaciones.repository.RepositorioPersonasAdministradoras;
import donaciones.service.AsignacionService;
import donaciones.service.DonacionService;
import donaciones.service.DonanteService;
import io.javalin.Javalin;

public class Main {
  public static void main(String[] args) {
    // Publicador de eventos y listeners
    RepositorioPersonasAdministradoras adminRepo = new RepositorioPersonasAdministradoras();
    PublicadorDeEventos publicador = new PublicadorDeEventos();
    publicador.suscribir(DonacionAsignadaEvent.class, new DonacionAsignadaListener());
    publicador.suscribir(InicioRutaEvent.class, new InicioRutaListener());
    publicador.suscribir(EntregaRealizadaEvent.class, new EntregaRealizadaListener());
    publicador.suscribir(EntregaNoSatisfactoriaEvent.class, new EntregaNoSatisfactoriaListener(adminRepo));

    DonacionRepository donacionesRepository = new DonacionRepository();
    RepositorioPersonas personasRepository = new RepositorioPersonas();
    DonacionService service = new DonacionService(donacionesRepository, personasRepository, publicador);
    DonacionController controller = new DonacionController(service);

    donaciones.repository.DonanteRepository donanteRepo = new donaciones.repository.DonanteRepository();
    DonanteService donanteService = new DonanteService(donanteRepo);
    DonanteController donanteController = new DonanteController(donanteService);

    EntidadBeneficiariaRepository entidadRepo = new EntidadBeneficiariaRepository();
    AsignacionService asignacionService = new AsignacionService(donacionesRepository, entidadRepo, publicador);
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
