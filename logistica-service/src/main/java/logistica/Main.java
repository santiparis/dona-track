package logistica;

import io.javalin.Javalin;
import logistica.client.RetrofitConfig;
import logistica.repository.RepositorioEntregas;
import logistica.repository.SeedEntregas;
import logistica.controller.DonacionesAPIController;
import logistica.service.DonacionesAPIService;

public class Main {
  public static void main(String[] args) {
    var repositorioEntregas = new RepositorioEntregas();
    var retrofitConfig = new RetrofitConfig();
    var donacionesAPIService = new DonacionesAPIService(repositorioEntregas, retrofitConfig.donacionesAPICalls());
    var entregaController = new DonacionesAPIController(donacionesAPIService);

    repositorioEntregas.agregarTodos(SeedEntregas.entregas());

    var app = Javalin.create(config -> {
      //health
      config.routes.get("/", ctx -> ctx.result("logistica-service OK"));

      //endpoints consumidos por el microservicio donaciones-service
      config.routes.post("/entregas", entregaController::crear);
      config.routes.get("/entregas/{id}", entregaController::obtener);
      config.routes.patch("/entregas/{id}/estado", entregaController::actualizarEstado);

    }).start(7070);
  }
}