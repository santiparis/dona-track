package logistica.mock;

import logistica.dto.LocalizacionDTO;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.PATCH;
import retrofit2.http.Path;

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

  // todos arrancan del deposito (aprox. Medrano, CABA); el trayecto hasta cada destino es por tierra
  private static final double LAT_DEPOSITO = -34.598;
  private static final double LON_DEPOSITO = -58.420;
  private static final int PASOS = 60; // 60 reportes de 5 segundos: llega en 5 minutos y vuelve a salir del deposito

  public static void main(String[] args) throws InterruptedException {
    int paso = 0;

    System.out.println("mock-gps: reportando ubicacion cada " + (INTERVALO_MS / 1000) + "s");

    while (true) {
      // los camiones de las rutas del seed (SeedCamiones), cada uno hacia su destino
      reportar("AB123CD", enCamino(paso, -34.4705, -59.4890)); // Escuela Rural N°10
      reportar("AE456FG", enCamino(paso, -34.3467, -58.7920)); // Comedor Escobar Sonrisas
      paso = (paso + 1) % (PASOS + 1);

      Thread.sleep(INTERVALO_MS);
    }
  }

  // simula el avance del camion sobre la ruta: en el paso 0 esta en el deposito y en el ultimo, en el destino
  private static LocalizacionDTO enCamino(int paso, double latDestino, double lonDestino) {
    double lat = LAT_DEPOSITO + (latDestino - LAT_DEPOSITO) * paso / PASOS;
    double lon = LON_DEPOSITO + (lonDestino - LON_DEPOSITO) * paso / PASOS;
    double velocidad = 30 + Math.random() * 30; // 30-60 km/h
    return new LocalizacionDTO(lat, lon, velocidad);
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