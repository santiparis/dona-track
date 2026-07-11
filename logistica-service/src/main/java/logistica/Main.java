package logistica;

import io.javalin.Javalin;
import logistica.client.RetrofitConfig;
import logistica.repository.RepositorioCamiones;
import logistica.repository.RepositorioEntregas;
import logistica.repository.SeedCamiones;
import logistica.repository.SeedEntregas;
import logistica.controller.DonacionesAPIController;
import logistica.service.DonacionesAPIService;

public class Main {
  public static void main(String[] args) {
    var repositorioEntregas = new RepositorioEntregas();
    var repositorioCamiones = new RepositorioCamiones();
    var retrofitConfig = new RetrofitConfig();
    var donacionesAPIService = new DonacionesAPIService(repositorioEntregas, retrofitConfig.donacionesAPICalls());
    var donacionesController = new DonacionesAPIController(donacionesAPIService);

    repositorioEntregas.agregarTodos(SeedEntregas.entregas());
    repositorioCamiones.agregarTodos(SeedCamiones.camiones());

    var app = Javalin.create(config -> {
      //health
      config.routes.get("/", ctx -> ctx.result("logistica-service OK"));

      // endpoints consumidos por el microservicio donaciones
      //agarro el conjunto de donaciones
      config.routes.post("/donaciones", ctx -> donacionesController.crear(ctx));


      config.routes.get("/donaciones/{id}", ctx -> donacionesController.obtener(ctx));
      config.routes.patch("/donaciones/{id}/estado", ctx -> donacionesController.actualizarEstado(ctx));

      //endpoints consumidos por el planificador externo
      //config.routes.post("/entregas",ctx -> planificadorController.crearEntregas(ctx));

    }).start(7070);
  }
}