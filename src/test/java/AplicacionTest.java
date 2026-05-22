import donante.*;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class AplicacionTest {
    private Aplicacion aplicacion;

    @BeforeEach
    void setUp() {
        aplicacion = new Aplicacion();
    }

    @Test
    void importarDonantesDesdeCSV_CreaNuevaPersonaHumana_SiNoExiste() throws IOException {
        // Arrange
        // Pre-condición: No hay donantes
        assertEquals(0, aplicacion.getDonantes().size());

        // Act
        aplicacion.importarDonantesDesdeCSV("donantes_solo_persona_humana.csv");

        // Assert
        assertEquals(6, aplicacion.getDonantes().size());

        // El donante existe
        Optional<Persona> donanteOpt = aplicacion.buscarDonantePorEmail("ananavarro3658@yahoo.com");
        assertTrue(donanteOpt.isPresent());

        // El donante es una persona humana
        Persona personaCreado = donanteOpt.get();
        assertInstanceOf(PersonaHumana.class, personaCreado);


        // Los datos del donante son correctos
        PersonaHumana personaHumana = (PersonaHumana) personaCreado;
        assertEquals(TipoDoc.DNI, personaHumana.getTipoDoc());
        assertEquals("28456905", personaHumana.getDocumento());
        assertEquals("Ana", personaHumana.getNombre());
        assertEquals("Navarro", personaHumana.getApellido());
        assertEquals("ananavarro3658@yahoo.com", personaHumana.getUsuario().getNombreUsuario());
    }

    @Test
    void importarDonantesDesdeCSV_CreaNuevaPersonaJuridica_SiNoExiste() throws IOException {
        // Arrange
        // Pre-condición: No hay donantes
        assertEquals(0, aplicacion.getDonantes().size());

        // Act
        aplicacion.importarDonantesDesdeCSV("donantes_solo_persona_juridica.csv");

        // Assert
        assertEquals(6, aplicacion.getDonantes().size());

        // El donante existe
        Optional<Persona> donanteOpt = aplicacion.buscarDonantePorEmail("santafeindustrial8180@yahoo.com");
        assertTrue(donanteOpt.isPresent());

        // El donante es una persona jurídica
        Persona personaCreado = donanteOpt.get();
        assertInstanceOf(PersonaJuridica.class, personaCreado);

        // Los datos del donante son correctos
        assertEquals(TipoDoc.CUIT, personaCreado.getTipoDoc());
        assertEquals("30-52235350-3", personaCreado.getDocumento());
        assertEquals("Santa Fe Industrial Fundación", personaCreado.getNombre());
        assertEquals("santafeindustrial8180@yahoo.com", personaCreado.getUsuario().getNombreUsuario());
    }

    @Test
    void importarDonantesDesdeCSV_ActualizaDonante_SiYaExiste() throws IOException {
        // Arrange: Pre-cargamos una persona humana en la aplicación
        List<Contacto> contactosAna = List.of(new Contacto(TipoContacto.EMAIL, "ananavarro3658@yahoo.com"));
        Usuario usuarioAna = new Usuario("ana_viejo", "passVieja");
        PersonaHumana donanteExistente = new PersonaHumana(
                "Nombre_viejo", "Apellido_viejo", 1, TipoDoc.DNI, "1", Genero.FEMENINO, "Av. Siempre Viva 742",
                contactosAna, contactosAna.get(0), usuarioAna
        );

        aplicacion.agregarDonante(donanteExistente);

        // Pre-condición: El donante existe con su nombre original
        assertEquals(1, aplicacion.getDonantes().size());

        // Act - El CSV contiene el mismo donante pero con datos actualizados
        aplicacion.importarDonantesDesdeCSV("donantes_solo_persona_humana.csv");

        // Assert
        assertEquals(6, aplicacion.getDonantes().size());

        // El donante debe seguir existiendo
        Optional<Persona> donanteOpt = aplicacion.buscarDonantePorEmail("ananavarro3658@yahoo.com");
        assertTrue(donanteOpt.isPresent());
        PersonaHumana donanteActualizado = (PersonaHumana) donanteOpt.get();

        // El donante existente debe tener los datos actualizados
        assertEquals(TipoDoc.DNI, donanteActualizado.getTipoDoc());
        assertEquals("28456905", donanteActualizado.getDocumento());
        assertEquals("Ana", donanteActualizado.getNombre());
        assertEquals("Navarro", donanteActualizado.getApellido());
        assertEquals("ananavarro3658@yahoo.com", donanteActualizado.getEmail());
    }
}