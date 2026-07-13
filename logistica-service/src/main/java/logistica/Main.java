package logistica;

import io.javalin.Javalin;
import logistica.retrofit_client.RetrofitConfig;
import logistica.repository.CamionesRepository;
import logistica.repository.DonacionesRepository;
import logistica.repository.RutasRepository;
import logistica.repository.SeedCamiones;
import logistica.controller.DonacionesAPIController;
import logistica.controller.EntregasController;
import logistica.controller.PlanificadorController;
import logistica.controller.RutasController;
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

    var planificadorService = new PlanificadorService(repositorioCamiones, repositorioRutas, repositorioDonaciones, retrofitConfig.planificadorAPICalls());
    var planificadorController = new PlanificadorController(planificadorService);

    repositorioCamiones.agregarTodos(SeedCamiones.camiones());

    var app = Javalin.create(config -> {
      //health
      config.routes.get("/", ctx -> ctx.result("logistica-service OK"));

      // endpoints consumidos por el microservicio donaciones
      config.routes.post("/donaciones", ctx -> donacionesController.obtenerDonaciones(ctx));

      //endpoints consumidos por el planificador externo
      config.routes.post("/rutas", ctx -> planificadorController.obtenerRutas(ctx));

      //lectura de una ruta (app del chofer: consulta el recorrido antes de iniciar)
      config.routes.get("/rutas/{id}", ctx -> rutasController.obtenerRuta(ctx));

      //endpoint que usa el chofer para iniciar su ruta
      config.routes.post("/rutas/{id}/iniciar", ctx -> rutasController.iniciarRuta(ctx));

      //lectura de una entrega (para que un objeto consulte el estado)
      config.routes.get("/entregas/{id}", ctx -> entregasController.obtenerEntrega(ctx));

      //endpoints que usa la entidad beneficiaria para confirmar/rechazar la recepcion
      config.routes.post("/entregas/{id}/confirmar", ctx -> entregasController.confirmar(ctx));
      config.routes.post("/entregas/{id}/no-recibida", ctx -> entregasController.marcarNoRecibida(ctx));
      config.routes.post("/entregas/{id}/reingresar-deposito", ctx -> entregasController.reingresarADeposito(ctx));

    }).start(7070);
  }
}