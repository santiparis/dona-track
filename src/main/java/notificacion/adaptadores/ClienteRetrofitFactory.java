package notificacion.adaptadores;

import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

public class ClienteRetrofitFactory {
    private static String baseUrl = "http://localhost:8089";
    private static PasarelaMensajeriaREST instancia;

    public static synchronized PasarelaMensajeriaREST getCliente() {
        if (instancia == null) {
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(baseUrl)
                    .addConverterFactory(JacksonConverterFactory.create())
                    .build();
            instancia = retrofit.create(PasarelaMensajeriaREST.class);
        }
        return instancia;
    }

    public static synchronized void setBaseUrl(String url) {
        baseUrl = url;
        instancia = null;
    }
}
