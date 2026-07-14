package donaciones.retrofit_client;

import donaciones.dto.DonacionLogisticaDTO;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

import java.util.List;

public interface LogisticaAPICalls {

  @POST("/donaciones")
  Call<String> enviarDonaciones(@Body List<DonacionLogisticaDTO> donaciones);
}