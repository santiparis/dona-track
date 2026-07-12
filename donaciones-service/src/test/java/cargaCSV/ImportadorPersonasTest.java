package cargaCSV;

import donaciones.domain.cargaCSV.ImportadorPersonas;
import donaciones.domain.cargaCSV.MapeoCsvEnPersonaException;
import donaciones.domain.donante.Contacto;
import donaciones.domain.donante.Genero;
import donaciones.domain.donante.Persona;
import donaciones.domain.donante.PersonaHumana;
import donaciones.domain.donante.PersonaJuridica;
import donaciones.domain.donante.RepositorioPersonas;
import donaciones.domain.donante.TipoDoc;
import donaciones.domain.donante.Usuario;

import donaciones.domain.notificacion.NotificacionPorEmail;
import donaciones.domain.notificacion.NotificacionPorWhatsApp;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ImportadorPersonasTest {

    @Mock
    private RepositorioPersonas repositorioPersonas;

    @Mock
    private NotificacionPorEmail estrategiaEmail;

    @Mock
    private NotificacionPorWhatsApp estrategiaWhatsapp;

    // Inyecto mocks para no enviar notificaciones reales
    @InjectMocks
    private ImportadorPersonas importador;

    @Test
    void procesarArchivoInvalidoLanzaExcepcionDeMapeo() {
        String pathCsvInvalido = "donantes_invalidos.csv";
        // La prueba verifica que el importador propaga la excepción correctamente.
        assertThrows(MapeoCsvEnPersonaException.class, () -> importador.importarPersonasDesdeCSV(pathCsvInvalido));
    }

    @Test
    void importarPersonasDesdeCSV_CreaNuevaPersonaHumana_SiNoExiste() {
        // Arrange
        String pathCsv = "donantes_solo_persona_humana.csv";
        // Mock: Cuando se busque por email, simular que no existe.|
        when(repositorioPersonas.buscarPorEmail(anyString())).thenReturn(Optional.empty());

        // Act
        importador.importarPersonasDesdeCSV(pathCsv);

        // Assert
        // Verificar que se intentó agregar 6 personas nuevas al repositorio
        verify(repositorioPersonas, times(6)).agregar(any(PersonaHumana.class));
    }

    @Test
    void importarPersonasDesdeCSV_CreaNuevaPersonaJuridica_SiNoExiste() {
        // Arrange
        String pathCsv = "donantes_solo_persona_juridica.csv";
        when(repositorioPersonas.buscarPorEmail(anyString())).thenReturn(Optional.empty());

        // Act
        importador.importarPersonasDesdeCSV(pathCsv);

        // Assert
        verify(repositorioPersonas, times(6)).agregar(any(PersonaJuridica.class));
    }

    @Test
    void importarPersonasDesdeCSV_ActualizaPersona_SiYaExiste() {
        String pathCsv = "donantes_solo_persona_humana.csv";
        NotificacionPorEmail estrategiaEmail = mock(NotificacionPorEmail.class);
        List<Contacto> contactosAna = List.of(new Contacto(estrategiaEmail, "ananavarro3658@yahoo.com"));
        Usuario usuarioAna = new Usuario("ana_viejo", "passVieja");
        PersonaHumana donanteExistente = spy(new PersonaHumana(
                "Nombre_viejo", "Apellido_viejo", 1, TipoDoc.DNI, "1", Genero.FEMENINO, "Av. Siempre Viva 742",
                contactosAna, contactosAna.get(0), usuarioAna));

        // Mock: Cuando se busque por el email de Ana, devolver la persona existente.
        // Para el resto, devolver vacío para que se creen como nuevas.
        when(repositorioPersonas.buscarPorEmail(anyString())).thenReturn(Optional.empty());
        when(repositorioPersonas.buscarPorEmail("ananavarro3658@yahoo.com")).thenReturn(Optional.of(donanteExistente));

        // Act
        importador.importarPersonasDesdeCSV(pathCsv);

        // Assert
        // Verificar que se llamó al método de actualización en la persona existente
        verify(donanteExistente).actualizarseDesde(any(Persona.class));

        // Verificar que se intentó agregar 5 personas (las 6 del CSV menos la que ya
        // existía)
        verify(repositorioPersonas, times(5)).agregar(any(Persona.class));
    }
}
