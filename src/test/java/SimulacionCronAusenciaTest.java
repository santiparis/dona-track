import donante.*;
import notificacion.*;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class SimulacionCronAusenciaTest {

    @Test
    public void testCrontabSimuladoDetectaInactividadYNotificaDirectamente() {
        EstrategiaDeNotificacion estrategiaMock = (destino, mensaje) -> true;

        Contacto contactoEmail1 = new Contacto(estrategiaMock, "ausente@donante.org");
        PersonaHumana donanteAusente = new PersonaHumana("Carlos Ausente", "Perez", 40, TipoDoc.DNI, "11111111", Genero.MASCULINO, "Calle 1", List.of(contactoEmail1), contactoEmail1, null);
        donanteAusente.setUltimaInteraccion(LocalDateTime.now().minusDays(25));

        Contacto contactoEmail2 = new Contacto(estrategiaMock, "activo@donante.org");
        PersonaHumana donanteActivo = new PersonaHumana("Ana Activa", "Lopez", 35, TipoDoc.DNI, "22222222", Genero.FEMENINO, "Calle 2", List.of(contactoEmail2), contactoEmail2, null);
        donanteActivo.setUltimaInteraccion(LocalDateTime.now().minusDays(5));

        List<Persona> usuariosRegistrados = List.of(donanteAusente, donanteActivo);
        List<Notificacion> historial = new ArrayList<>();

        // --- Ejecución simulada del Demonio Crontab nocturno ---
        LocalDateTime umbralInactividad = LocalDateTime.now().minusDays(20);
        for (Persona usuario : usuariosRegistrados) {
            if (usuario.getUltimaInteraccion().isBefore(umbralInactividad)) {
                Notificacion notif = usuario.notificar("¡Hola " + usuario.getNombre() + "! Notamos más de 20 días sin actividad. ¡Te invitamos a realizar una nueva donación en la plataforma!");
                if (notif != null) {
                    historial.add(notif);
                }
            }
        }

        assertEquals(1, historial.size(), "Debe haberse generado exactamente una notificación para el usuario ausente");
        assertTrue(historial.get(0).getMensaje().contains("Carlos Ausente"), "El mensaje debe estar personalizado para Carlos Ausente");
    }
}
