package donaciones.domain.notificacion;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NotificacionPorSMS implements EstrategiaDeNotificacion {

    private static final Logger logger = LoggerFactory.getLogger(NotificacionPorSMS.class);

    @Override
    public boolean enviar(String destino, String mensaje) {
        String accountSid = System.getenv("TWILIO_ACCOUNT_SID");
        String authToken = System.getenv("TWILIO_AUTH_TOKEN");
        String fromNumber = System.getenv("TWILIO_PHONE_NUMBER");

        // Modo simulado: sin credenciales se registra el envío y se retorna éxito, para no
        // consumir cuota externa ni exigir configuración al correr mvn test o la importación masiva.
        if (estaSinConfigurar(accountSid) || estaSinConfigurar(authToken) || estaSinConfigurar(fromNumber)) {
            logger.info("[SIMULADO] SMS a {}: {}", destino, mensaje);
            return true;
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

    private boolean estaSinConfigurar(String variableDeEntorno) {
        return variableDeEntorno == null || variableDeEntorno.trim().isEmpty();
    }
}