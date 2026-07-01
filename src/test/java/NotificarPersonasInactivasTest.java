import donaciones.domain.donante.Contacto;
import donaciones.domain.donante.Genero;
import donaciones.domain.donante.Persona;
import donaciones.domain.donante.PersonaHumana;
import donaciones.domain.donante.TipoDoc;
import donaciones.domain.notificacion.EstrategiaDeNotificacion;

import donaciones.domain.notificacion.Notificacion;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import planificacion.NotificarPersonasInactivas;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class NotificarPersonasInactivasTest {

    private static List<Persona> personasRegistradas;

    @BeforeAll
    public static void setUp() {
        EstrategiaDeNotificacion estrategiaMock = (destino, mensaje) -> true;

        Contacto contactoEmail1 = new Contacto(estrategiaMock, "ausente@donante.org");
        PersonaHumana donanteAusente = new PersonaHumana("Carlos Ausente", "Perez", 40, TipoDoc.DNI, "11111111", Genero.MASCULINO, "Calle 1", List.of(contactoEmail1), contactoEmail1, null);
        donanteAusente.setUltimaInteraccion(LocalDateTime.now().minusDays(25));

        Contacto contactoEmail2 = new Contacto(estrategiaMock, "activo@donante.org");
        PersonaHumana donanteActivo = new PersonaHumana("Ana Activa", "Lopez", 35, TipoDoc.DNI, "22222222", Genero.FEMENINO, "Calle 2", List.of(contactoEmail2), contactoEmail2, null);
        donanteActivo.setUltimaInteraccion(LocalDateTime.now().minusDays(5));

        personasRegistradas = List.of(donanteAusente, donanteActivo);
    }

    @Test
    public void testCrontabSimuladoDetectaInactividadYNotificaDirectamente() {
        List<Notificacion> historial = NotificarPersonasInactivas.notificarInactivos(personasRegistradas);

        assertEquals(1, historial.size(), "Debe haberse generado exactamente una notificación para la persona ausente");
    }
}
