package donaciones.retrofit_client;

import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

public class RetrofitConfig {

  private String logisticaBaseUrl = "http://localhost:7070/";

  public void setLogisticaBaseUrl(String url) {
    this.logisticaBaseUrl = url;
  }

  public LogisticaAPICalls logisticaAPICalls() {
    return construirRetrofit(logisticaBaseUrl)
        .create(LogisticaAPICalls.class);
  }

  private Retrofit construirRetrofit(String baseUrl) {
    return new Retrofit.Builder()
        .baseUrl(baseUrl)
        .addConverterFactory(JacksonConverterFactory.create())
        .build();
  }
}