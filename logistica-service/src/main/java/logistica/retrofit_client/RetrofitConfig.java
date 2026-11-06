package logistica.retrofit_client;

import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;
import okhttp3.OkHttpClient;

public class RetrofitConfig {

  private String donacionesTokenInterno = System.getenv("DONACIONES_TOKEN_INTERNO");

  private String donacionesBaseUrl =
      System.getenv().getOrDefault(
          "DONACIONES_URL", "http://localhost:8081/"
      );

  private String planificadorBaseUrl =
      System.getenv().getOrDefault(
          "PLANIFICADOR_URL", "http://localhost:9090/"
      );

  public void setDonacionesBaseUrl(String url) {
    this.donacionesBaseUrl = url;
  }

  public void setPlanificadorBaseUrl(String url) {
    this.planificadorBaseUrl = url;
  }

  public void setDonacionesTokenInterno(String token) {
    this.donacionesTokenInterno = token;
  }

  public DonacionesAPICalls donacionesAPICalls() {
    OkHttpClient cliente = new OkHttpClient.Builder()
        // No reenviar la credencial si la API responde con una redirección.
        .followRedirects(false)
        .followSslRedirects(false)
        .addInterceptor(chain -> {
          var request = chain.request();
          if (donacionesTokenInterno != null && !donacionesTokenInterno.isBlank()) {
            request = request.newBuilder().header("X-Token-Interno", donacionesTokenInterno).build();
          }
          return chain.proceed(request);
        })
        .build();
    return construirRetrofit(donacionesBaseUrl).client(cliente).build().create(DonacionesAPICalls.class);
  }

  public PlanificadorAPICalls planificadorAPICalls() {
    return construirRetrofit(planificadorBaseUrl).build().create(PlanificadorAPICalls.class);
  }

  private Retrofit.Builder construirRetrofit(String baseUrl) {
    return new Retrofit.Builder()
        .baseUrl(baseUrl)
        .addConverterFactory(JacksonConverterFactory.create());
  }
}
