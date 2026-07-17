import donaciones.domain.donante.Contacto;
import donaciones.domain.notificacion.EnvioDeWhatsAppException;
import donaciones.domain.notificacion.EstadoNotificacion;
import donaciones.domain.notificacion.Notificacion;
import donaciones.domain.notificacion.EnvioDeEmailException;
import donaciones.domain.notificacion.EnvioDeSMSException;
import donaciones.domain.notificacion.EstrategiaDeNotificacion;
import donaciones.domain.notificacion.NotificacionPorEmail;
import donaciones.domain.notificacion.NotificacionPorSMS;
import donaciones.domain.notificacion.NotificacionPorWhatsApp;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class NotificacionTest {

    private EstrategiaDeNotificacion estrategiaMock;

    @BeforeEach
    void setUp() {
        estrategiaMock = mock(EstrategiaDeNotificacion.class);
        when(estrategiaMock.enviar(anyString(), anyString())).thenReturn(true);
    }

    @Test
    public void testEnvioDeNotificacionSmsConEstrategiaMock() {
        Contacto contactoSms = new Contacto(estrategiaMock, "+5491122334455");
        String mensaje = "Código de verificación DonaTrack: 4819";
        Notificacion notificacion = new Notificacion(contactoSms, mensaje);
        notificacion.enviar();

        assertEquals(EstadoNotificacion.COMPLETADA, notificacion.getEstado(), "La notificación SMS debe marcarse como completada exitosamente");
        verify(estrategiaMock, times(1)).enviar(eq("+5491122334455"), eq(mensaje));
    }

    @Test
    public void testEnvioDeNotificacionWhatsAppConEstrategiaMock() {
        Contacto contactoWa = new Contacto(estrategiaMock, "+5491199887766");
        String mensaje = "¡Hola! Tu envío está en camino.";
        Notificacion notificacion = new Notificacion(contactoWa, mensaje);
        notificacion.enviar();

        assertEquals(EstadoNotificacion.COMPLETADA, notificacion.getEstado(), "La notificación WhatsApp debe completarse");
        verify(estrategiaMock, times(1)).enviar(eq("+5491199887766"), eq(mensaje));
    }

    @Test
    public void testNotificacionAEntidadBeneficiariaMedianteServicio() {
        EntidadBeneficiaria entidad = new EntidadBeneficiaria("Fundación Esperanza", "Calle 123", "445566", List.of("contacto@esperanza.org"));
        Contacto contactoWa = new Contacto(estrategiaMock, "+5491100001111");
        entidad.registrarContacto(contactoWa, true);

        String mensaje = "Aviso para entidad: Se le ha asignado satisfactoriamente una nueva donación.";
        Notificacion notif = entidad.notificar(mensaje);

        assertEquals(EstadoNotificacion.COMPLETADA, notif.getEstado());
        verify(estrategiaMock, times(1)).enviar(eq("+5491100001111"), eq(mensaje));
    }

    @Test
    public void testEnvioDeEmailsDeBienvenida() {
        Contacto contactoUsuario1 = new Contacto(estrategiaMock, "juanignaciopereyra01@gmail.com");
        Contacto contactoUsuario2 = new Contacto(estrategiaMock, "parispueblasantiago@gmail.com");

        String emailDeBienvenida = "¡Bienvenido a DonaTrack! Gracias por sumarte a nuestra plataforma.";
        Notificacion notif1 = new Notificacion(contactoUsuario1, emailDeBienvenida);
        Notificacion notif2 = new Notificacion(contactoUsuario2, emailDeBienvenida);

        notif1.enviar();
        notif2.enviar();

        assertEquals(EstadoNotificacion.COMPLETADA, notif1.getEstado(), "El correo al primer usuario debe enviarse exitosamente");
        assertEquals(EstadoNotificacion.COMPLETADA, notif2.getEstado(), "El correo al segundo usuario debe enviarse exitosamente");
        verify(estrategiaMock, times(1)).enviar(eq("juanignaciopereyra01@gmail.com"), eq(emailDeBienvenida));
        verify(estrategiaMock, times(1)).enviar(eq("parispueblasantiago@gmail.com"), eq(emailDeBienvenida));
    }

    @Test
    public void testLanzaEnvioDeEmailExceptionAnteFalloDeEnvio() {
        NotificacionPorEmail estrategiaMock = mock(NotificacionPorEmail.class);
        when(estrategiaMock.enviar(anyString(), anyString()))
                .thenThrow(new EnvioDeEmailException("Error simulado al enviar por SendGrid"));

        Contacto contactoConError = new Contacto(estrategiaMock, "fallo@donatrack.org");

        Notificacion notif = new Notificacion(contactoConError, "Mensaje que fallará");

        assertThrows(EnvioDeEmailException.class, notif::enviar,
                "Debe lanzarse EnvioDeEmailException cuando el envío de correo falla.");
    }

    @Test
    public void testNotificacionPorSms() {
        NotificacionPorSMS estrategiaSms = mock(NotificacionPorSMS.class);
        when(estrategiaSms.enviar(anyString(), anyString())).thenReturn(true);

        Contacto contactoSms = new Contacto(estrategiaSms, "+54119837462");
        Notificacion notificacion = new Notificacion(contactoSms, "Mensaje SMS de prueba mockeada");
        notificacion.enviar();

        assertEquals(EstadoNotificacion.COMPLETADA, notificacion.getEstado(),
                "Debe completarse exitosamente la notificación SMS cuando la estrategia mockeada retorna true.");
        verify(estrategiaSms, times(1)).enviar(eq("+54119837462"), eq("Mensaje SMS de prueba mockeada"));
    }

    @Test
    public void testNotificacionPorWhatsApp() {
        NotificacionPorWhatsApp estrategiaWa = mock(NotificacionPorWhatsApp.class);
        when(estrategiaWa.enviar(anyString(), anyString())).thenReturn(true);

        Contacto contactoWa = new Contacto(estrategiaWa, "+54119837462");
        Notificacion notificacion = new Notificacion(contactoWa, "Mensaje WhatsApp de prueba mockeada");
        notificacion.enviar();

        assertEquals(EstadoNotificacion.COMPLETADA, notificacion.getEstado(),
                "Debe completarse exitosamente la notificación WhatsApp cuando la estrategia mockeada retorna true.");
        verify(estrategiaWa, times(1)).enviar(eq("+54119837462"), eq("Mensaje WhatsApp de prueba mockeada"));
    }


    @Test
    public void testLanzaEnvioDeSMSExceptionAnteFalloDeEnvio() {
        NotificacionPorSMS estrategiaMock = mock(NotificacionPorSMS.class);
        when(estrategiaMock.enviar(anyString(), anyString()))
                .thenThrow(new EnvioDeSMSException("Error simulado al enviar por Twilio SMS"));

        Contacto contactoConError = new Contacto(estrategiaMock, "+54119837462");
        Notificacion notif = new Notificacion(contactoConError, "Mensaje SMS que fallará");

        assertThrows(EnvioDeSMSException.class, notif::enviar,
                "Debe lanzarse EnvioDeSMSException cuando el envío por SMS falla.");
    }

    @Test
    public void testLanzaEnvioDeWhatsAppExceptionAnteFalloDeEnvio() {
        NotificacionPorWhatsApp estrategiaMock = mock(NotificacionPorWhatsApp.class);
        when(estrategiaMock.enviar(anyString(), anyString()))
                .thenThrow(new EnvioDeWhatsAppException("Error simulado al enviar por Twilio WhatsApp"));

        Contacto contactoConError = new Contacto(estrategiaMock, "+54119837462");
        Notificacion notif = new Notificacion(contactoConError, "Mensaje WhatsApp que fallará");

        assertThrows(EnvioDeWhatsAppException.class, notif::enviar,
                "Debe lanzarse EnvioDeWhatsAppException cuando el envío por WhatsApp falla.");
    }
}
