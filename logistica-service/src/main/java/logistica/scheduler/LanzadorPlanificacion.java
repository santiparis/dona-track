package logistica.scheduler;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

/**
 * Tarea calendarizada (crontab del SO) que dispara la planificacion de rutas.
 *
 * No hace el trabajo: le pega por HTTP al logistica-service que esta corriendo,
 * porque los repos (donaciones, camiones) viven en memoria en ESE proceso.
 * Hace una ejecucion y termina -> pensado para ser invocado por cron
 * (ej: 0 3 * * *  java -jar lanzador-planificacion.jar).
 */
public class LanzadorPlanificacion {

  private static final String URL = "http://localhost:7070/planificar";

  public static void main(String[] args) {
    try {
      HttpRequest request = HttpRequest.newBuilder(URI.create(URL))
          .POST(HttpRequest.BodyPublishers.noBody())
          .build();
      HttpResponse<String> response = HttpClient.newHttpClient()
          .send(request, HttpResponse.BodyHandlers.ofString());
      System.out.println("lanzador-planificacion: " + response.statusCode() + " " + response.body());
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    } catch (Exception e) {
      System.err.println("lanzador-planificacion: fallo al disparar -> " + e.getMessage());
    }
  }
}