import donante.Contacto;
import donante.TipoContacto;
import notificacion.ServicioDeNotificacion;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class ServicioDeNotificacionTest {

    private ServicioDeNotificacion servicioDeNotificacion;
    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;

    @BeforeEach
    void setUp() {
        servicioDeNotificacion = new ServicioDeNotificacion();
        System.setOut(new PrintStream(outContent));
    }

    @Test
    void testEnviarEmail() {
        Contacto contacto = new Contacto(TipoContacto.EMAIL, "test@example.com");
        servicioDeNotificacion.enviar(contacto, "Mensaje de prueba por email");
        assertTrue(outContent.toString().contains("Simulando envío de correo electrónico a: test@example.com"));
    }

    @Test
    void testEnviarSMS() {
        Contacto contacto = new Contacto(TipoContacto.SMS, "123456789");
        servicioDeNotificacion.enviar(contacto, "Mensaje de prueba por SMS");
        assertTrue(outContent.toString().contains("Simulando envío de SMS al teléfono: 123456789"));
    }

    @Test
    void testEnviarWhatsapp(){
        Contacto contacto = new Contacto(TipoContacto.WHATSAPP, "123456789");
        servicioDeNotificacion.enviar(contacto, "Mensaje de prueba por Whatsapp");
        assertTrue(outContent.toString().contains("Simulando envío de WhatsApp al teléfono: 123456789"));
    }

    @AfterEach
    void restoreStreams() {
        System.setOut(originalOut);
    }
}