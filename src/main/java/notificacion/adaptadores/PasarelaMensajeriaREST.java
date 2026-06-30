package notificacion.adaptadores;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface PasarelaMensajeriaREST {
    record MensajePayload(String destino, String contenido) {}
    record RespuestaPayload(String estado, String id) {}

    @POST("/sms")
    Call<RespuestaPayload> enviarSMS(@Body MensajePayload payload);

    @POST("/whatsapp")
    Call<RespuestaPayload> enviarWhatsApp(@Body MensajePayload payload);
}
