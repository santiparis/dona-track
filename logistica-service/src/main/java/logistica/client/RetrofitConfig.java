package logistica.client;

import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

public class RetrofitConfig {

  private String donacionesBaseUrl = "http://localhost:8080/";
  private String planificadorBaseUrl = "http://localhost:9090/";

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