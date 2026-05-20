import notificacion.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class NotificadorTest {

    private Notificador notificador;

    @BeforeEach
    void setUp() {

    }

    @Test
    void testNotificacionPorEmail() {
        Notificacion notificacion = new Notificacion("test@test.com", "Prueba de email");
        assertEquals(EstadoNotificacion.PENDIENTE, notificacion.getEstado());

        notificador = new Notificador(new NotificacionPorEmail());
        notificador.enviar(notificacion);

        assertEquals(EstadoNotificacion.COMPLETADA, notificacion.getEstado());
    }

    @Test
    void testNotificacionPorSMS() {
        Notificacion notificacion = new Notificacion("11 6589 8465", "Prueba de SMS");
        assertEquals(EstadoNotificacion.PENDIENTE, notificacion.getEstado());

        notificador = new Notificador(new NotificacionPorSMS());
        notificador.enviar(notificacion);

        assertEquals(EstadoNotificacion.COMPLETADA, notificacion.getEstado());
    }

    @Test
    void testNotificacionPorWhatsApp() {
        Notificacion notificacion = new Notificacion("11 6589 8465", "Prueba de WhatsApp");
        assertEquals(EstadoNotificacion.PENDIENTE, notificacion.getEstado());

        notificador = new Notificador(new NotificacionPorWhatsApp());
        notificador.enviar(notificacion);

        assertEquals(EstadoNotificacion.COMPLETADA, notificacion.getEstado());
    }

    @Test
    void testCambioDeEstrategia() {
        Notificacion notificacionEmail = new Notificacion("test@test.com", "Primer mensaje por email");
        Notificacion notificacionSms = new Notificacion("11 6589 8465", "Segundo mensaje por SMS");

        // Iniciar con Email
        notificador = new Notificador(new NotificacionPorEmail());
        notificador.enviar(notificacionEmail);
        assertEquals(EstadoNotificacion.COMPLETADA, notificacionEmail.getEstado());

        // Cambiar a SMS
        notificador.setEstrategia(new NotificacionPorSMS());
        notificador.enviar(notificacionSms);
        assertEquals(EstadoNotificacion.COMPLETADA, notificacionSms.getEstado());
    }
}