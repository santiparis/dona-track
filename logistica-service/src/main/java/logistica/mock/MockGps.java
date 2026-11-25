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
   no necesita Javalin, solo Retrofit + un loop periodico.
 */
public class MockGps {

  // contrato de integracion: el endpoint de ingesta que expone logistica
  interface LogisticaGps {
    @PATCH("api/camiones/{patente}/localizacion")
    Call<Void> reportar(@Path("patente") String patente, @Body LocalizacionDTO reporte);
  }

  private static final int INTERVALO_MS = 5000; // el device reporta cada 5 segundos

  private static final LogisticaGps API = new Retrofit.Builder()
      .baseUrl(System.getenv().getOrDefault("LOGISTICA_URL", "http://localhost:7070/"))
      .addConverterFactory(JacksonConverterFactory.create())
      .build()
      .create(LogisticaGps.class);

  // patentes del seed de camiones; cada una parte del deposito y "se mueve"
  private static final List<String> PATENTES = List.of("AB123CD", "XY987ZW");

  public static void main(String[] args) throws InterruptedException {
    // arrancan todos desde el deposito (aprox. Medrano, CABA) hacia el destino de la ruta del seed
    // (Escuela Rural N°10, San Andres de Giles): todo el trayecto es por tierra
    double latDeposito = -34.598;
    double lonDeposito = -58.420;
    double latDestino = -34.4705;
    double lonDestino = -59.4890;
    int pasos = 300; // llega en 25 minutos y vuelve a salir del deposito
    int paso = 0;

    System.out.println("mock-gps: reportando ubicacion cada " + (INTERVALO_MS / 1000) + "s");

    while (true) {
      // simula el avance del camion sobre la ruta
      double lat = latDeposito + (latDestino - latDeposito) * paso / pasos;
      double lon = lonDeposito + (lonDestino - lonDeposito) * paso / pasos;
      paso = (paso + 1) % (pasos + 1);
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