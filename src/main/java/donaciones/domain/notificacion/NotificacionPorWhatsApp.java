package donaciones.domain.notificacion;

import donaciones.domain.notificacion.adaptadores.ClienteRetrofitFactory;
import donaciones.domain.notificacion.adaptadores.PasarelaMensajeriaREST;
import retrofit2.Response;

public class NotificacionPorWhatsApp implements EstrategiaDeNotificacion {
    @Override
    public boolean enviar(String destino, String mensaje) {
        try {
            PasarelaMensajeriaREST cliente = ClienteRetrofitFactory.getCliente();
            Response<PasarelaMensajeriaREST.RespuestaPayload> response = cliente
                    .enviarWhatsApp(new PasarelaMensajeriaREST.MensajePayload(destino, mensaje))
                    .execute();
            return response.isSuccessful();
        } catch (Exception e) {
            System.err.println("[WHATSAPP ERROR] " + e.getMessage());
            return false;
        }
    }
}