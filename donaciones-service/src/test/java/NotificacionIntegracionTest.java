import donaciones.domain.EntidadBeneficiaria;
import donaciones.domain.donante.Contacto;
import donaciones.domain.notificacion.EstadoNotificacion;
import donaciones.domain.notificacion.Notificacion;
import donaciones.domain.notificacion.EnvioDeEmailException;
import donaciones.domain.notificacion.EstrategiaDeNotificacion;
import donaciones.domain.notificacion.NotificacionPorEmail;
import donaciones.domain.notificacion.NotificacionPorSMS;
import donaciones.domain.notificacion.NotificacionPorWhatsApp;
import donaciones.domain.notificacion.adaptadores.ClienteRetrofitFactory;
import io.javalin.Javalin;
import java.util.List;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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
    

    @Test
    public void testEnvioDeEmailsDeBienvenida() {
        Contacto contactoUsuario1 = new Contacto(new NotificacionPorEmail(), "juanignaciopereyra01@gmail.com");
        Contacto contactoUsuario2 = new Contacto(new NotificacionPorEmail(), "parispueblasantiago@gmail.com");

        String emailDeBienvenida = "¡Bienvenido a DonaTrack! Gracias por sumarte a nuestra plataforma.";
        Notificacion notif1 = new Notificacion(contactoUsuario1, emailDeBienvenida);
        Notificacion notif2 = new Notificacion(contactoUsuario2, emailDeBienvenida);

        notif1.enviar();
        notif2.enviar();

        assertEquals(EstadoNotificacion.COMPLETADA, notif1.getEstado(), "El correo al primer usuario debe enviarse exitosamente");
        assertEquals(EstadoNotificacion.COMPLETADA, notif2.getEstado(), "El correo al segundo usuario debe enviarse exitosamente");
    }

    @Test
    public void testLanzaEnvioDeEmailExceptionAnteFalloDeEnvio() {
        EstrategiaDeNotificacion estrategiaMock = mock(EstrategiaDeNotificacion.class);
        when(estrategiaMock.enviar(anyString(), anyString()))
                .thenThrow(new EnvioDeEmailException("Error simulado al enviar por SendGrid"));

        Contacto contactoConError = new Contacto(estrategiaMock, "fallo@donatrack.org");

        Notificacion notif = new Notificacion(contactoConError, "Mensaje que fallará");

        assertThrows(EnvioDeEmailException.class, () -> notif.enviar(),
                "Debe lanzarse EnvioDeEmailException cuando el envío de correo falla.");
    }
}
