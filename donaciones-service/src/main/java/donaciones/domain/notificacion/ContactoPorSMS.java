package donaciones.domain.notificacion;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Entity
@DiscriminatorValue("SMS")
public class ContactoPorSMS extends Contacto {
    protected ContactoPorSMS() { }

    private static final Logger logger = LoggerFactory.getLogger(ContactoPorSMS.class);

    public ContactoPorSMS(String valor) {
        super(valor);
    }

    public boolean enviar(String mensaje) {
        String accountSid = System.getenv("TWILIO_ACCOUNT_SID");
        String authToken = System.getenv("TWILIO_AUTH_TOKEN");
        String fromNumber = System.getenv("TWILIO_PHONE_NUMBER");
        // La cuenta de prueba de Twilio solo entrega SMS de un segmento (160 caracteres sin acentos):
        // el SMS avisa que hay algo nuevo y el mensaje completo queda en la bandeja de la web
        String aviso = "DonaTrack: tenes un aviso nuevo. Miralo en "
            + Objects.requireNonNullElse(System.getenv("DONACIONES_URL_PUBLICA"), "http://localhost:8081") + "/login";

        // Modo simulado: sin credenciales se registra el envío y se retorna éxito, para no
        // consumir cuota externa ni exigir configuración al correr mvn test.
        if (estaSinConfigurar(accountSid) || estaSinConfigurar(authToken) || estaSinConfigurar(fromNumber)) {
            logger.info("[SIMULADO] SMS a {}: {}", getValor(), aviso);
            return true;
        }

        try {
            Twilio.init(accountSid, authToken);
            Message message = Message.creator(
                    new PhoneNumber(getValor()),
                    new PhoneNumber(fromNumber),
                    aviso
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
