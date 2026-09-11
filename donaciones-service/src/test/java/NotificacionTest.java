import donaciones.domain.EntidadBeneficiaria;
import donaciones.domain.donante.Genero;
import donaciones.domain.donante.PersonaHumana;
import donaciones.domain.donante.TipoDoc;
import donaciones.domain.notificacion.Contacto;
import donaciones.domain.notificacion.EstadoNotificacion;
import donaciones.domain.notificacion.Notificacion;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class NotificacionTest {

    private Contacto contactoQueEnvia;
    private Contacto contactoQueFalla;

    @BeforeEach
    void setUp() {
        contactoQueEnvia = mock(Contacto.class);
        when(contactoQueEnvia.enviar(anyString())).thenReturn(true);

        contactoQueFalla = mock(Contacto.class);
        when(contactoQueFalla.enviar(anyString())).thenReturn(false);
    }

    private PersonaHumana personaCon(List<Contacto> contactos, Contacto medioPredeterminado) {
        return new PersonaHumana(
            "Ana", "Pérez", 30, TipoDoc.DNI, "12345678", Genero.FEMENINO,
            "Medrano 951", contactos, medioPredeterminado, null
        );
    }

    @Test
    public void testLaNotificacionQuedaCompletadaCuandoElEnvioSeConcreta() {
        PersonaHumana persona = personaCon(List.of(contactoQueEnvia), contactoQueEnvia);
        String mensaje = "Código de verificación DonaTrack: 4819";

        Notificacion notificacion = persona.notificar(mensaje);

        assertEquals(EstadoNotificacion.COMPLETADA, notificacion.getEstado(),
                "La notificación debe completarse cuando el contacto concreta el envío");
        verify(contactoQueEnvia, times(1)).enviar(eq(mensaje));
    }

    @Test
    public void testLaNotificacionQuedaFallidaCuandoNoSeConcretaElEnvio() {
        PersonaHumana persona = personaCon(List.of(contactoQueFalla), contactoQueFalla);

        Notificacion notificacion = persona.notificar("Mensaje que fallará");

        assertEquals(EstadoNotificacion.FALLIDA, notificacion.getEstado(),
                "La notificación debe quedar FALLIDA cuando el contacto no concreta el envío");
    }

    @Test
    public void testSeNotificaPorElMedioPredeterminadoYNoPorLosDemas() {
        Contacto otroContacto = mock(Contacto.class);
        PersonaHumana persona = personaCon(List.of(otroContacto, contactoQueEnvia), contactoQueEnvia);
        String mensaje = "Su donación fue asignada";

        persona.notificar(mensaje);

        verify(contactoQueEnvia, times(1)).enviar(eq(mensaje));
        verify(otroContacto, never()).enviar(anyString());
    }

    // Persona exige al menos un contacto y un medio predeterminado válido, así que los casos sin
    // medio predeterminado sólo se pueden dar en una EntidadBeneficiaria, que no tiene esa invariante.
    @Test
    public void testSeNotificaPorElPrimerContactoSiNoHayMedioPredeterminado() {
        Contacto otroContacto = mock(Contacto.class);
        EntidadBeneficiaria entidad = new EntidadBeneficiaria(
            "Comedor Los Niños", "Calle 456", "112233", List.of("comedor@mail.org"),
            List.of(contactoQueEnvia, otroContacto), null);
        String mensaje = "Su donación fue asignada";

        Notificacion notificacion = entidad.notificar(mensaje);

        assertEquals(EstadoNotificacion.COMPLETADA, notificacion.getEstado());
        verify(contactoQueEnvia, times(1)).enviar(eq(mensaje));
        verify(otroContacto, never()).enviar(anyString());
    }

    @Test
    public void testNoSeGeneraNotificacionSiElReceptorNoTieneContactos() {
        EntidadBeneficiaria entidadSinContactos = new EntidadBeneficiaria(
            "Escuela Rural N°10", "Ruta 8 km 60", "445566", List.of("escuela@mail.org"));

        assertNull(entidadSinContactos.notificar("Mensaje sin destino"),
                "Sin ningún contacto no hay forma de notificar, no debe generarse la notificación");
    }

    @Test
    public void testSeNotificaAUnaEntidadBeneficiaria() {
        EntidadBeneficiaria entidad = new EntidadBeneficiaria(
            "Fundación Esperanza", "Calle 123", "445566", List.of("contacto@esperanza.org"));
        entidad.registrarContacto(contactoQueEnvia, true);
        String mensaje = "Se le ha asignado satisfactoriamente una nueva donación.";

        Notificacion notificacion = entidad.notificar(mensaje);

        assertEquals(EstadoNotificacion.COMPLETADA, notificacion.getEstado());
        verify(contactoQueEnvia, times(1)).enviar(eq(mensaje));
    }

    @Test
    public void testLaNotificacionRegistraElMensajeEnviado() {
        PersonaHumana persona = personaCon(List.of(contactoQueEnvia), contactoQueEnvia);
        String mensaje = "¡Bienvenido a DonaTrack! Gracias por sumarte a nuestra plataforma.";

        Notificacion notificacion = persona.notificar(mensaje);

        assertEquals(mensaje, notificacion.getMensaje());
    }
}
