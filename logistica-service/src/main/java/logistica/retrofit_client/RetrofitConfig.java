package logistica.retrofit_client;

import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

public class RetrofitConfig {

  private String donacionesBaseUrl =
      System.getenv().getOrDefault(
          "DONACIONES_URL", "http://localhost:8081/"
      );

  private String planificadorBaseUrl =
      System.getenv().getOrDefault(
          "PLANIFICADOR_URL", "http://localhost:9092/"
      );

  public void setDonacionesBaseUrl(String url) {
    this.donacionesBaseUrl = url;
  }

  public void setPlanificadorBaseUrl(String url) {
    this.planificadorBaseUrl = url;
  }

  public DonacionesAPICalls donacionesAPICalls() {
    return construirRetrofit(donacionesBaseUrl).create(DonacionesAPICalls.class);
  }

  public PlanificadorAPICalls planificadorAPICalls() {
    return construirRetrofit(planificadorBaseUrl).create(PlanificadorAPICalls.class);
  }

  private Retrofit construirRetrofit(String baseUrl) {
    return new Retrofit.Builder()
        .baseUrl(baseUrl)
        .addConverterFactory(JacksonConverterFactory.create())
        .build();
  }
}
