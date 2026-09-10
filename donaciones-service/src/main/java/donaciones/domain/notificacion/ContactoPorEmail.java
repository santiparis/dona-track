package donaciones.domain.notificacion;

import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ContactoPorEmail extends Contacto {

    private static final Logger logger = LoggerFactory.getLogger(ContactoPorEmail.class);

    public ContactoPorEmail(String valor) {
        super(valor);
    }

    public boolean enviar(String mensaje) {
        String remitente = System.getenv("SENDGRID_REMITENTE");
        String apiKey = System.getenv("SENDGRID_API_KEY");

        // Modo simulado: sin credenciales se registra el envío y se retorna éxito, para no
        // consumir cuota externa ni exigir configuración al correr mvn test o la importación masiva.
        if (estaSinConfigurar(remitente) || estaSinConfigurar(apiKey)) {
            logger.info("[SIMULADO] Email a {}: {}", getValor(), mensaje);
            return true;
        }

        // Para enviar mails realmente hay que tener la apikey cargada en el sengrind.env
        // Y correr este comando en la terminal: source sendgrid.env 
        Email from = new Email(remitente);
        String subject = "Notificación del sistema DonaTrack";
        Email to = new Email(getValor());
        Content content = new Content("text/plain", mensaje);
        Mail mail = new Mail(from, subject, to, content);

        SendGrid sg = new SendGrid(apiKey);
        Request request = new Request();
        try {
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());
            Response response = sg.api(request);
            int statusCode = response.getStatusCode();
            if (statusCode >= 200 && statusCode < 300) {
                return true;
            } else {
                logger.error("SendGrid respondió con código HTTP {}: {}", statusCode, response.getBody());
                return false;
            }
            // Uso IOException porque las clases de SendGrid lanzan esta excepción
        } catch (IOException ex) {
            logger.error("Fallo de red o I/O al comunicarse con SendGrid: {}", ex.getMessage());
            return false;
        }
    }

    private boolean estaSinConfigurar(String variableDeEntorno) {
        return variableDeEntorno == null || variableDeEntorno.trim().isEmpty();
    }
}