package notificacion;

import notificacion.adaptadores.ClienteRetrofitFactory;
import notificacion.adaptadores.PasarelaMensajeriaREST;
import retrofit2.Response;

public class NotificacionPorSMS implements EstrategiaDeNotificacion {
    @Override
    public boolean enviar(String destino, String mensaje) {
        try {
            PasarelaMensajeriaREST cliente = ClienteRetrofitFactory.getCliente();
            Response<PasarelaMensajeriaREST.RespuestaPayload> response = cliente
                    .enviarSMS(new PasarelaMensajeriaREST.MensajePayload(destino, mensaje))
                    .execute();
            return response.isSuccessful();
        } catch (Exception e) {
            System.err.println("[SMS ERROR] " + e.getMessage());
            return false;
        }
    }
}