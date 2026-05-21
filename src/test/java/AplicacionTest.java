import donante.Contacto;
import donante.Donante;
import donante.Genero;
import donante.RazonSocial;
import donante.TipoContacto;
import donante.Usuario;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import donante.PersonaHumana;
import donante.PersonaJuridica;
import donante.TipoDoc;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class AplicacionTest {
    private Aplicacion aplicacion;
    private PersonaHumana donanteHumano;
    private PersonaJuridica donanteJuridico;

    @BeforeEach
    void setUp() {
        aplicacion = new Aplicacion();
        List<Contacto> contactos = List.of(new Contacto(TipoContacto.EMAIL, "test@test.com"));
        Usuario usuario = new Usuario("user", "pass");
        donanteHumano = new PersonaHumana("Nombre", "Apellido", 30, TipoDoc.DNI, "12345678", Genero.MASCULINO, "Calle Falsa 123", contactos, contactos.get(0), usuario);
        donanteJuridico = new PersonaJuridica(TipoDoc.CUIT, "30-12345678-9", "Empresa S.A.", RazonSocial.EMPRESA, "Tecnología", Collections.emptyList(), contactos, contactos.get(0), usuario);
    }

    @Test
    void sePuedeAgregarUnaPersonaHumana() {
        // Arrange
        assertEquals(0, aplicacion.getDonantes().size());

        // Act
        aplicacion.agregarDonante(donanteHumano);

        // Assert
        assertEquals(1, aplicacion.getDonantes().size());
        Optional<Donante> donanteRecuperado = aplicacion.buscarDonantePorDocumento("12345678");
        assertTrue(donanteRecuperado.isPresent());
        assertEquals("Nombre", donanteRecuperado.get().getNombre());
    }

    @Test
    void sePuedeAgregarUnaPersonaJuridica() {
        // Arrange
        assertEquals(0, aplicacion.getDonantes().size());

        // Act
        aplicacion.agregarDonante(donanteJuridico);

        // Assert
        assertEquals(1, aplicacion.getDonantes().size());
        Optional<Donante> donanteRecuperado = aplicacion.buscarDonantePorDocumento("30-12345678-9");
        assertTrue(donanteRecuperado.isPresent());
        assertEquals("Empresa S.A.", donanteRecuperado.get().getNombre());
    }

    @Test
    void sePuedeActualizarUnaPersonaHumana() {
        // Arrange
        aplicacion.agregarDonante(donanteHumano);
        List<Contacto> contactos = List.of(new Contacto(TipoContacto.EMAIL, "test@test.com"));
        Usuario usuario = new Usuario("user", "pass");
        PersonaHumana donanteActualizado = new PersonaHumana("NombreActualizado", "Apellido", 30, TipoDoc.DNI, "12345678", Genero.MASCULINO, "Calle Falsa 123", contactos, contactos.get(0), usuario);
        
        // Act
        aplicacion.actualizarDonante("12345678", donanteActualizado);

        // Assert
        Optional<Donante> donanteRecuperado = aplicacion.buscarDonantePorDocumento("12345678");
        assertTrue(donanteRecuperado.isPresent());
        assertEquals("NombreActualizado", donanteRecuperado.get().getNombre());
    }

    @Test
    void sePuedeActualizarUnaPersonaJuridica() {
        // Arrange
        aplicacion.agregarDonante(donanteJuridico);
        List<Contacto> contactos = List.of(new Contacto(TipoContacto.EMAIL, "test@test.com"));
        Usuario usuario = new Usuario("user", "pass");
        PersonaJuridica donanteActualizado = new PersonaJuridica(TipoDoc.CUIT, "30-12345678-9", "Empresa Actualizada S.A.", RazonSocial.EMPRESA, "Tecnología", Collections.emptyList(), contactos, contactos.get(0), usuario);

        // Act
        aplicacion.actualizarDonante("30-12345678-9", donanteActualizado);

        // Assert
        Optional<Donante> donanteRecuperado = aplicacion.buscarDonantePorDocumento("30-12345678-9");
        assertTrue(donanteRecuperado.isPresent());
        assertEquals("Empresa Actualizada S.A.", donanteRecuperado.get().getNombre());
    }

    @Test
    void sePuedeEliminarUnaPersonaHumana() {
        // Arrange
        aplicacion.agregarDonante(donanteHumano);
        assertEquals(1, aplicacion.getDonantes().size());

        // Act
        aplicacion.eliminarDonante("12345678");

        // Assert
        assertEquals(0, aplicacion.getDonantes().size());
        Optional<Donante> donanteRecuperado = aplicacion.buscarDonantePorDocumento("12345678");
        assertTrue(donanteRecuperado.isEmpty());
    }

    @Test
    void sePuedeEliminarUnaPersonaJuridica() {
        // Arrange
        aplicacion.agregarDonante(donanteJuridico);
        assertEquals(1, aplicacion.getDonantes().size());

        // Act
        aplicacion.eliminarDonante("30-12345678-9");

        // Assert
        assertEquals(0, aplicacion.getDonantes().size());
        Optional<Donante> donanteRecuperado = aplicacion.buscarDonantePorDocumento("30-12345678-9");
        assertTrue(donanteRecuperado.isEmpty());
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
        Optional<Donante> donanteOpt = aplicacion.buscarDonantePorEmail("ananavarro3658@yahoo.com");
        assertTrue(donanteOpt.isPresent());

        // El donante es una persona humana
        Donante donanteCreado = donanteOpt.get();
        assertInstanceOf(PersonaHumana.class, donanteCreado);


        // Los datos del donante son correctos
        PersonaHumana personaHumana = (PersonaHumana) donanteCreado;
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
        Optional<Donante> donanteOpt = aplicacion.buscarDonantePorEmail("santafeindustrial8180@yahoo.com");
        assertTrue(donanteOpt.isPresent());

        // El donante es una persona jurídica
        Donante donanteCreado = donanteOpt.get();
        assertInstanceOf(PersonaJuridica.class, donanteCreado);

        // Los datos del donante son correctos
        assertEquals(TipoDoc.CUIT, donanteCreado.getTipoDoc());
        assertEquals("30-52235350-3", donanteCreado.getDocumento());
        assertEquals("Santa Fe Industrial Fundación", donanteCreado.getNombre());
        assertEquals("santafeindustrial8180@yahoo.com", donanteCreado.getUsuario().getNombreUsuario());
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
        Optional<Donante> donanteOpt = aplicacion.buscarDonantePorEmail("ananavarro3658@yahoo.com");
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