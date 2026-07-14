import donaciones.domain.EntidadBeneficiaria;
import donaciones.domain.donante.Contacto;
import donaciones.domain.notificacion.EstadoNotificacion;
import donaciones.domain.notificacion.Notificacion;
import donaciones.domain.notificacion.EnvioDeEmailException;
import donaciones.domain.notificacion.EstrategiaDeNotificacion;
import donaciones.domain.notificacion.NotificacionPorEmail;
import java.util.List;
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
        EntidadBeneficiaria entidad = new EntidadBeneficiaria(1L, "Fundación Esperanza", "Calle 123", "445566", List.of("contacto@esperanza.org"));
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
}
