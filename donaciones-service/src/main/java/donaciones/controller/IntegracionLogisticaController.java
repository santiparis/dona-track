package donaciones.controller;

import io.javalin.http.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IntegracionLogisticaController {
  private static final Logger logger = LoggerFactory.getLogger(IntegracionLogisticaController.class);

  public void rutasIniciadas(Context ctx) {
    logger.info("Callback recibido: rutas iniciadas");
    logger.debug("Body callback rutas iniciadas: {}", ctx.body());
    ctx.status(200).result("OK");
  }

  public void entregaCompletada(Context ctx) {
    logger.info("Callback recibido: entrega completada");
    logger.debug("Body callback entrega completada: {}", ctx.body());
    ctx.status(200).result("OK");
  }

  public void entregaFallida(Context ctx) {
    logger.info("Callback recibido: entrega fallida");
    logger.debug("Body callback entrega fallida: {}", ctx.body());
    ctx.status(200).result("OK");
  }
}
