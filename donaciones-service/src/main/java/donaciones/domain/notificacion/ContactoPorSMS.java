package donaciones.domain.notificacion;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ContactoPorSMS extends Contacto {

    private static final Logger logger = LoggerFactory.getLogger(ContactoPorSMS.class);

    public ContactoPorSMS(String valor) {
        super(valor);
    }

    public boolean enviar(String mensaje) {
        String accountSid = System.getenv("TWILIO_ACCOUNT_SID");
        String authToken = System.getenv("TWILIO_AUTH_TOKEN");
        String fromNumber = System.getenv("TWILIO_PHONE_NUMBER");

        // Modo simulado: sin credenciales se registra el envío y se retorna éxito, para no
        // consumir cuota externa ni exigir configuración al correr mvn test o la importación masiva.
        if (estaSinConfigurar(accountSid) || estaSinConfigurar(authToken) || estaSinConfigurar(fromNumber)) {
            logger.info("[SIMULADO] SMS a {}: {}", getValor(), mensaje);
            return true;
        }

        try {
            Twilio.init(accountSid, authToken);
            Message message = Message.creator(
                    new PhoneNumber(getValor()),
                    new PhoneNumber(fromNumber),
                    mensaje
            ).create();
            return message != null && message.getSid() != null;
        } catch (RuntimeException ex) {
            logger.error("Fallo al comunicarse con Twilio para envío de SMS: {}", ex.getMessage());
            return false;
        }
    }

    private boolean estaSinConfigurar(String variableDeEntorno) {
        return variableDeEntorno == null || variableDeEntorno.trim().isEmpty();
    }
}