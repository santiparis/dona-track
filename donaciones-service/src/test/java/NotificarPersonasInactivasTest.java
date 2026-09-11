import donaciones.domain.donante.Genero;
import donaciones.domain.donante.PersonaHumana;
import donaciones.domain.donante.RepositorioPersonas;
import donaciones.domain.donante.TipoDoc;
import donaciones.domain.notificacion.ContactoPorEmail;

import donaciones.domain.notificacion.Notificacion;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import planificacion.NotificarPersonasInactivas;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class NotificarPersonasInactivasTest {

    private static RepositorioPersonas repositorioPersonas;

    @BeforeAll
    public static void setUp() {
        ContactoPorEmail contactoMock = mock(ContactoPorEmail.class);
        when(contactoMock.enviar(anyString())).thenReturn(true);

        PersonaHumana donanteAusente = new PersonaHumana("Carlos Ausente", "Perez", 40, TipoDoc.DNI, "11111111", Genero.MASCULINO, "Calle 1", List.of(contactoMock), contactoMock, null);
        donanteAusente.setUltimaInteraccion(LocalDateTime.now().minusDays(25));

        PersonaHumana donanteActivo = new PersonaHumana("Ana Activa", "Lopez", 35, TipoDoc.DNI, "22222222", Genero.FEMENINO, "Calle 2", List.of(contactoMock), contactoMock, null);
        donanteActivo.setUltimaInteraccion(LocalDateTime.now().minusDays(5));

        repositorioPersonas = new RepositorioPersonas();
        repositorioPersonas.agregar(donanteAusente);
        repositorioPersonas.agregar(donanteActivo);
    }

    @Test
    public void testCrontabSimuladoDetectaInactividadYNotificaDirectamente() {
        List<Notificacion> historial = NotificarPersonasInactivas.notificarInactivos(repositorioPersonas);

        assertEquals(1, historial.size(), "Debe haberse generado exactamente una notificación para la persona ausente");
    }
}
