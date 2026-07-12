package donaciones.domain.notificacion;

import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import java.io.IOException;

public class NotificacionPorEmail implements EstrategiaDeNotificacion {

    @Override
    public boolean enviar(String destino, String mensaje) {
        String remitente = System.getenv("SENDGRID_REMITENTE");
        String apiKey = System.getenv("SENDGRID_API_KEY");

        // Si el remitente o la API Key no están definidos, lanza una excepción
        if (remitente == null || remitente.trim().isEmpty() || apiKey == null || apiKey.trim().isEmpty()) {
            throw new ConfiguracionSendGridException("No están configuradas las variables de entorno SENDGRID_REMITENTE y/o SENDGRID_API_KEY.");
        }

        // Para enviar mails realmente hay que tener la apikey cargada en el sengrind.env
        // Y correr este comando en la terminal: source sendgrid.env 
        Email from = new Email(remitente);
        String subject = "Notificación del sistema DonaTrack";
        Email to = new Email(destino);
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
                throw new EnvioDeEmailException("SendGrid respondió con código HTTP " + statusCode + ": " + response.getBody());
            }
            // Uso IOException porque las clases de SendGrid lanzan esta excepción
        } catch (IOException ex) {
            throw new EnvioDeEmailException("Fallo de red o I/O al comunicarse con SendGrid: " + ex.getMessage());
        }
    }
}