package logistica.client;

import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

public class RetrofitConfig {

  private final Retrofit retrofit = new Retrofit.Builder()
      .baseUrl("https://localhost:8080/")
      .addConverterFactory(JacksonConverterFactory.create())
      .build();

  public DonacionesAPICalls donacionesAPICalls() {
    return retrofit.create(DonacionesAPICalls.class);
  }

  public PlanificadorAPICalls planificadorAPICalls() {
    return retrofit.create(PlanificadorAPICalls.class);
  }


}