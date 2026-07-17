package logistica;

import io.javalin.Javalin;
import logistica.controller.CamionesController;
import logistica.controller.DonacionesAPIController;
import logistica.controller.EntregasController;
import logistica.controller.PlanificadorController;
import logistica.controller.RutasController;
import logistica.retrofit_client.RetrofitConfig;
import logistica.repository.CamionesRepository;
import logistica.repository.DonacionesRepository;
import logistica.repository.RutasRepository;
import logistica.repository.SeedCamiones;
import logistica.service.CamionesService;
import logistica.service.DonacionesService;
import logistica.service.EntregasService;
import logistica.service.PlanificadorService;

public class Main {
  public static void main(String[] args) {
    var repositorioCamiones = new CamionesRepository();
    var repositorioDonaciones = new DonacionesRepository();
    var repositorioRutas = new RutasRepository();
    var retrofitConfig = new RetrofitConfig();

    var entregasService = new EntregasService(repositorioRutas, retrofitConfig.donacionesAPICalls());
    var donacionesService = new DonacionesService(repositorioDonaciones);
    var donacionesController = new DonacionesAPIController(donacionesService);
    var rutasController = new RutasController(entregasService);
    var entregasController = new EntregasController(entregasService);
    var camionesService = new CamionesService(repositorioCamiones);
    var camionesController = new CamionesController(camionesService);

    var planificadorService = new PlanificadorService(repositorioCamiones, repositorioRutas, repositorioDonaciones, retrofitConfig.planificadorAPICalls());
    var planificadorController = new PlanificadorController(planificadorService);

    repositorioCamiones.agregarTodos(SeedCamiones.camiones());

    var app = Javalin.create(config -> {
      //health
      config.routes.get("/", ctx -> ctx.result("logistica-service OK"));

      // Gestión de flota de camiones
      config.routes.get("/api/camiones", camionesController::getCamiones);
      config.routes.get("/api/camiones/{id}", camionesController::getCamion);
      config.routes.post("/api/camiones", camionesController::postCamiones);
      config.routes.delete("/api/camiones/{id}", camionesController::deleteCamion);
      config.routes.put("/api/camiones/{id}", camionesController::putCamion);

      // Actualización de la localización de un camión
      config.routes.patch("/api/camiones/{id}/localizacion", camionesController::actualizarLocalizacion);

      // endpoints consumidos por el microservicio donaciones
      config.routes.post("/api/donaciones", donacionesController::obtenerDonaciones);

      //dispara la planificacion (lo llama el cron via HTTP, o a demanda)
      config.routes.post("/api/planificaciones", planificadorController::planificar);

      //endpoints consumidos por el planificador externo (callback con las rutas armadas)
      config.routes.post("/api/rutas", planificadorController::obtenerRutas);

      // listado general de rutas planificadas
      config.routes.get("/rutas", rutasController::obtenerRutas);

      //lectura de una ruta (app del chofer: consulta el recorrido antes de iniciar)
      config.routes.get("/api/rutas/{id}", rutasController::obtenerRuta);

      //endpoint que usa el chofer para iniciar su ruta
      config.routes.post("/api/rutas/{id}/inicio", rutasController::iniciarRuta);

      //lectura de una entrega (para que un objeto consulte el estado)
      config.routes.get("/api/entregas/{id}", entregasController::obtenerEntrega);

      //endpoints que usa la entidad beneficiaria para confirmar/rechazar la recepcion
      config.routes.post("/api/entregas/{id}/confirmacion", entregasController::confirmar);
      config.routes.post("/api/entregas/{id}/rechazo", entregasController::marcarNoRecibida);
      config.routes.post("/api/entregas/{id}/reingreso", entregasController::reingresarADeposito);

    }).start(7070);
  }
}
