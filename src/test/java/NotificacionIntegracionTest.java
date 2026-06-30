import donante.Contacto;
import io.javalin.Javalin;
import notificacion.*;
import notificacion.adaptadores.ClienteRetrofitFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class NotificacionIntegracionTest {
    private static Javalin app;
    private static final int PORT = 8199;

    @BeforeAll
    public static void setupServer() {
        app = Javalin.create().start(PORT);
        app.post("/sms", ctx -> ctx.json("{\"estado\": \"ENVIADO\", \"id\": \"SMS-101\"}"));
        app.post("/whatsapp", ctx -> ctx.json("{\"estado\": \"ENVIADO\", \"id\": \"WA-202\"}"));
        ClienteRetrofitFactory.setBaseUrl("http://localhost:" + PORT);
    }

    @AfterAll
    public static void stopServer() {
        if (app != null) {
            app.stop();
        }
    }

    @Test
    public void testEnvioRealSmsMedianteRetrofitYJavalin() {
        Contacto contactoSms = new Contacto(new NotificacionPorSMS(), "+5491122334455");
        Notificacion notificacion = new Notificacion(contactoSms, "Código de verificación DonaTrack: 4819");
        notificacion.enviar();

        assertEquals(EstadoNotificacion.COMPLETADA, notificacion.getEstado(), "La notificación SMS debe marcarse como completada exitosamente");
    }

    @Test
    public void testEnvioRealWhatsAppMedianteRetrofitYJavalin() {
        Contacto contactoWa = new Contacto(new NotificacionPorWhatsApp(), "+5491199887766");
        Notificacion notificacion = new Notificacion(contactoWa, "¡Hola! Tu envío está en camino.");
        notificacion.enviar();

        assertEquals(EstadoNotificacion.COMPLETADA, notificacion.getEstado(), "La notificación WhatsApp debe completarse");
    }

    @Test
    public void testNotificacionAEntidadBeneficiariaMedianteServicio() {
        EntidadBeneficiaria entidad = new EntidadBeneficiaria("Fundación Esperanza", "Calle 123", "445566", List.of("contacto@esperanza.org"));
        Contacto contactoWa = new Contacto(new NotificacionPorWhatsApp(), "+5491100001111");
        entidad.registrarContacto(contactoWa, true);

        Notificacion notif = entidad.notificar("Aviso para entidad: Se le ha asignado satisfactoriamente una nueva donación.");

        assertEquals(EstadoNotificacion.COMPLETADA, notif.getEstado());
    }
}
