package logistica.mock;

import logistica.dto.LocalizacionDTO;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.PATCH;
import retrofit2.http.Path;

import java.util.List;

/**
 * Mock del dispositivo GPS instalado en el camion (actor EXTERNO de campo).
 * Corre como proceso aparte y simula el hardware real: mientras esta encendido,
 * cada pocos segundos reporta por HTTP su ubicacion y velocidad al logistica-service.
 *
 * A diferencia del MockPlanificador, este es un cliente PURO: solo emite, nadie
 * le pega. Por eso no necesita Javalin, solo Retrofit + un loop periodico.
 * En produccion, este componente lo provee y configura el equipo de dispositivos.
 */
public class MockGps {

  // contrato de integracion: el endpoint de ingesta que expone logistica
  interface LogisticaGps {
    @PATCH("api/camiones/{patente}/localizacion")
    Call<Void> reportar(@Path("patente") String patente, @Body LocalizacionDTO reporte);
  }

  private static final int INTERVALO_MS = 5000; // el device reporta cada 5 segundos

  private static final LogisticaGps API = new Retrofit.Builder()
      .baseUrl("http://localhost:7070/")
      .addConverterFactory(JacksonConverterFactory.create())
      .build()
      .create(LogisticaGps.class);

  // patentes del seed de camiones; cada una parte del deposito y "se mueve"
  private static final List<String> PATENTES = List.of("AB123CD", "XY987ZW");

  public static void main(String[] args) throws InterruptedException {
    // arrancan todos desde el deposito (aprox. Medrano, CABA)
    double lat = -34.598;
    double lon = -58.420;

    System.out.println("mock-gps: reportando ubicacion cada " + (INTERVALO_MS / 1000) + "s");

    while (true) {
      // simula el avance del camion sobre la ruta
      lat += 0.001;
      lon += 0.001;
      double velocidad = 30 + Math.random() * 30; // 30-60 km/h

      for (String patente : PATENTES) {
        reportar(patente, new LocalizacionDTO(lat, lon, velocidad));
      }

      Thread.sleep(INTERVALO_MS);
    }
  }

  private static void reportar(String patente, LocalizacionDTO reporte) {
    try {
      int code = API.reportar(patente, reporte).execute().code();
      System.out.printf("mock-gps: %s -> (%.4f, %.4f) %.0f km/h [%d]%n",
          patente, reporte.latitud(), reporte.longitud(), reporte.velocidad(), code);
    } catch (Exception e) {
      System.err.println("mock-gps: fallo al reportar " + patente + " -> " + e.getMessage());
    }
  }
}