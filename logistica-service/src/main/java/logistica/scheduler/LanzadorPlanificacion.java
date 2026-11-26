package logistica.scheduler;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * Tarea calendarizada (crontab del SO) que dispara la planificacion de rutas.
 *
 * No hace el trabajo: le pega por HTTP al logistica-service que esta corriendo,
 * porque los repos (donaciones, camiones) viven en memoria en ESE proceso.
 * Hace una ejecucion y termina -> pensado para ser invocado por cron
 * (ej: 0 3 * * *  java -jar lanzador-planificacion.jar).
 */
public class LanzadorPlanificacion {

  private static final String URL =
      System.getenv().getOrDefault(
          "LOGISTICA_URL",
          "http://localhost:7070"
      ) + "/api/planificaciones";

  public static void main(String[] args) {
    try {
      HttpRequest request = HttpRequest.newBuilder(URI.create(URL))
          .timeout(Duration.ofSeconds(20))
          .POST(HttpRequest.BodyPublishers.noBody())
          .build();
      HttpResponse<String> response = HttpClient.newHttpClient()
          .send(request, HttpResponse.BodyHandlers.ofString());
      System.out.println("lanzador-planificacion: " + response.statusCode() + " " + response.body());
      if (response.statusCode() < 200 || response.statusCode() >= 300) System.exit(1);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      System.err.println("lanzador-planificacion: ejecucion interrumpida");
      System.exit(1);
    } catch (Exception e) {
      System.err.println("lanzador-planificacion: fallo al disparar -> " + e.getMessage());
      System.exit(1);
    }
  }
}
