package donaciones.domain.notificacion;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;

public class NotificacionPorSMS implements EstrategiaDeNotificacion {

    @Override
    public boolean enviar(String destino, String mensaje) {
        String accountSid = System.getenv("TWILIO_ACCOUNT_SID");
        String authToken = System.getenv("TWILIO_AUTH_TOKEN");
        String fromNumber = System.getenv("TWILIO_PHONE_NUMBER");

        if (accountSid == null || accountSid.trim().isEmpty() || authToken == null || authToken.trim().isEmpty()
                || fromNumber == null || fromNumber.trim().isEmpty()) {
            throw new ConfiguracionTwilioException("No están configuradas las variables de entorno TWILIO_ACCOUNT_SID, TWILIO_AUTH_TOKEN y/o TWILIO_PHONE_NUMBER.");
        }

        try {
            Twilio.init(accountSid, authToken);
            Message message = Message.creator(
                    new PhoneNumber(destino),
                    new PhoneNumber(fromNumber),
                    mensaje
            ).create();
            return message != null && message.getSid() != null;
        } catch (Exception ex) {
            throw new EnvioDeSMSException("Fallo al comunicarse con Twilio para envío de SMS: " + ex.getMessage());
        }
    }
}