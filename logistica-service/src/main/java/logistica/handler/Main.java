package logistica.handler;

import io.javalin.Javalin;
import logistica.domain.RepositorioEntregas;
import logistica.handler.DonacionesAPIHandler;

public class Main {
  public static void main(String[] args) {
    var repositorioEntregas = new RepositorioEntregas();
    var entregaHandler = new DonacionesAPIHandler(repositorioEntregas);

    var app = Javalin.create(config -> {
      //health
      config.routes.get("/", ctx -> ctx.result("logistica-service OK"));

      //endpoints consumidos por donaciones-service
      config.routes.post("/entregas", entregaHandler::crear);
      config.routes.get("/entregas/{id}", entregaHandler::obtener);
      config.routes.patch("/entregas/{id}/estado", entregaHandler::actualizarEstado);

    }).start(7070);
  }
}