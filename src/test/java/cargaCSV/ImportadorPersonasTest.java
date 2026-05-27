package cargaCSV;

import donante.*;
import donante.RepositorioPersonas;
import notificacion.ServicioDeNotificacion;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Collections;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class ImportadorPersonasTest {

    private static List<Persona> personas;
    private static ImportadorPersonas importador;
    private static RepositorioPersonas repositorioPersonas;
    private static ServicioDeNotificacion servicioDeNotificacion;
    private static PersonaHumana donanteHumano;
    private static PersonaJuridica donanteJuridico;
    private static final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;

    @BeforeEach
    void setUp() {
        // Se ejecuta una sola vez para toda la clase, ideal para archivos grandes.
        String pathCsv = "donantes_import_20000_UTF8_BOM.csv";
        LectorArchivoCsv lectorArchivoCsv = new LectorArchivoCsv(pathCsv);
        personas = lectorArchivoCsv.procesarCsv();

        // Aplicacion
        repositorioPersonas = new RepositorioPersonas();
        servicioDeNotificacion = new ServicioDeNotificacion();
        importador = new ImportadorPersonas(repositorioPersonas, servicioDeNotificacion);
        List<Contacto> contactos = List.of(new Contacto(TipoContacto.EMAIL, "test@test.com"));
        Usuario usuario = new Usuario("user", "pass");
        donanteHumano = new PersonaHumana("Nombre", "Apellido", 30, TipoDoc.DNI, "12345678", Genero.MASCULINO, "Calle Falsa 123", contactos, contactos.get(0), usuario);
        donanteJuridico = new PersonaJuridica(TipoDoc.CUIT, "30-12345678-9", "Empresa S.A.", RazonSocial.EMPRESA, "Tecnología", Collections.emptyList(), contactos, contactos.get(0), usuario);
        System.setOut(new PrintStream(outContent));
    }

    @Test
    void procesarDevuelveLaCantidadCorrectaDeDonantes() {
        assertFalse(personas.isEmpty(), "La lista de donantes no debería estar vacía.");
        assertEquals(20000, personas.size(), "Debería haber 20,000 donantes en la lista.");
    }

    @Test
    void mapeaPersonaHumanaCorrectamente() {
        // Buscamos un donante que sabemos que está
        Optional<Persona> donanteOpt = personas.stream()
                .filter(d -> d.getDocumento().equals("28456905"))
                .findFirst();

        assertTrue(donanteOpt.isPresent(), "Debería encontrar a la persona humana por DNI.");
        Persona donante = donanteOpt.get();
        assertInstanceOf(PersonaHumana.class, donante, "El donante debería ser una PersonaHumana.");
        PersonaHumana persona = (PersonaHumana) donante;

        assertEquals("Ana", persona.getNombre());
        assertEquals("Navarro", persona.getApellido());
        assertEquals(TipoDoc.DNI, persona.getTipoDoc());
    }

    @Test
    void mapeaPersonaJuridicaCorrectamente() {
        // Buscamos una persona jurídica que sabemos que está
        Optional<Persona> donanteOpt = personas.stream()
                .filter(d -> d.getDocumento().equals("30-52235350-3"))
                .findFirst();

        assertTrue(donanteOpt.isPresent(), "Debería encontrar a la persona jurídica por CUIT.");
        Persona persona = donanteOpt.get();
        assertInstanceOf(PersonaJuridica.class, persona, "El donante debería ser una PersonaJuridica.");

        assertEquals("Santa Fe Industrial Fundación", persona.getNombre());
        assertEquals(TipoDoc.CUIT, persona.getTipoDoc());
    }

    @Test
    void creaContactosCorrectamenteParaUnaPersona() {
        // Buscamos el mismo donante para verificar sus contactos
        Optional<Persona> donanteOpt = personas.stream()
                .filter(d -> d.getDocumento().equals("28456905"))
                .findFirst();
        
        assertTrue(donanteOpt.isPresent());
        Persona persona = donanteOpt.get();

        assertNotNull(persona.getContactos());
        // El archivo grande puede tener o no teléfono para esta persona, seamos flexibles
        assertFalse(persona.getContactos().isEmpty(), "Debería tener al menos 1 contacto (email).");

        Optional<Contacto> emailOpt = persona.getContactos().stream()
                .filter(c -> c.getTipo() == TipoContacto.EMAIL)
                .findFirst();
        assertTrue(emailOpt.isPresent());
        assertEquals("ananavarro3658@yahoo.com", emailOpt.get().getValor());

        // Verificar medio predeterminado
        assertNotNull(persona.getMedioPredeterminado());
        assertEquals(TipoContacto.EMAIL, persona.getMedioPredeterminado().getTipo());
    }

    @Test
    void procesarArchivoInvalidoLanzaExcepcionDeMapeo() {
        String pathCsvInvalido = "donantes_invalidos.csv";
        ImportadorPersonas importadorInvalido = new ImportadorPersonas(new RepositorioPersonas(), new ServicioDeNotificacion());

        // Verificamos que al procesar el archivo inválido, se lanza nuestra excepción custom.
        assertThrows(MapeoCsvEnPersonaException.class, () -> {
            importadorInvalido.importarPersonasDesdeCSV(pathCsvInvalido);
        });
    }


    @AfterEach
    void restoreStreams() {
        System.setOut(originalOut);
    }

    @Test
    void sePuedeAgregarUnaPersonaHumana() {
        // Arrange
        assertEquals(0, repositorioPersonas.obtenerTodas().size());

        // Act
        repositorioPersonas.agregar(donanteHumano);

        // Assert
        assertEquals(1, repositorioPersonas.obtenerTodas().size());
        Optional<Persona> donanteRecuperado = repositorioPersonas.buscarPorDocumento("12345678");
        assertTrue(donanteRecuperado.isPresent());
        assertEquals("Nombre", donanteRecuperado.get().getNombre());
    }

    @Test
    void sePuedeAgregarUnaPersonaJuridica() {
        // Arrange
        assertEquals(0, repositorioPersonas.obtenerTodas().size());

        // Act
        repositorioPersonas.agregar(donanteJuridico);

        // Assert
        assertEquals(1, repositorioPersonas.obtenerTodas().size());
        Optional<Persona> donanteRecuperado = repositorioPersonas.buscarPorDocumento("30-12345678-9");
        assertTrue(donanteRecuperado.isPresent());
        assertEquals("Empresa S.A.", donanteRecuperado.get().getNombre());
    }

    @Test
    void sePuedeActualizarUnaPersonaHumana() {
        // Arrange
        repositorioPersonas.agregar(donanteHumano);
        List<Contacto> contactos = List.of(new Contacto(TipoContacto.EMAIL, "test@test.com"));
        Usuario usuario = new Usuario("user", "pass");
        PersonaHumana donanteActualizado = new PersonaHumana("NombreActualizado", "Apellido", 30, TipoDoc.DNI, "12345678", Genero.MASCULINO, "Calle Falsa 123", contactos, contactos.get(0), usuario);

        // Act
        repositorioPersonas.buscarPorDocumento("12345678").ifPresent(existente -> existente.actualizarseDesde(donanteActualizado));

        // Assert
        Optional<Persona> donanteRecuperado = repositorioPersonas.buscarPorDocumento("12345678");
        assertTrue(donanteRecuperado.isPresent());
        assertEquals("NombreActualizado", donanteRecuperado.get().getNombre());
    }

    @Test
    void sePuedeActualizarUnaPersonaJuridica() {
        // Arrange
        repositorioPersonas.agregar(donanteJuridico);
        List<Contacto> contactos = List.of(new Contacto(TipoContacto.EMAIL, "test@test.com"));
        Usuario usuario = new Usuario("user", "pass");
        PersonaJuridica donanteActualizado = new PersonaJuridica(TipoDoc.CUIT, "30-12345678-9", "Empresa Actualizada S.A.", RazonSocial.EMPRESA, "Tecnología", Collections.emptyList(), contactos, contactos.get(0), usuario);

        // Act
        repositorioPersonas.buscarPorDocumento("30-12345678-9").ifPresent(existente -> existente.actualizarseDesde(donanteActualizado));

        // Assert
        Optional<Persona> donanteRecuperado = repositorioPersonas.buscarPorDocumento("30-12345678-9");
        assertTrue(donanteRecuperado.isPresent());
        assertEquals("Empresa Actualizada S.A.", donanteRecuperado.get().getNombre());
    }

    @Test
    void sePuedeEliminarUnaPersonaHumana() {
        // Arrange
        repositorioPersonas.agregar(donanteHumano);
        assertEquals(1, repositorioPersonas.obtenerTodas().size());

        // Act
        repositorioPersonas.eliminarPorDocumento("12345678");

        // Assert
        assertEquals(0, repositorioPersonas.obtenerTodas().size());
        Optional<Persona> donanteRecuperado = repositorioPersonas.buscarPorDocumento("12345678");
        assertTrue(donanteRecuperado.isEmpty());
    }

    @Test
    void sePuedeEliminarUnaPersonaJuridica() {
        // Arrange
        repositorioPersonas.agregar(donanteJuridico);
        assertEquals(1, repositorioPersonas.obtenerTodas().size());

        // Act
        repositorioPersonas.eliminarPorDocumento("30-12345678-9");

        // Assert
        assertEquals(0, repositorioPersonas.obtenerTodas().size());
        Optional<Persona> donanteRecuperado = repositorioPersonas.buscarPorDocumento("30-12345678-9");
        assertTrue(donanteRecuperado.isEmpty());
    }

    @Test
    void importarPersonasDesdeCSV_CreaNuevaPersonaHumana_SiNoExiste(){
        // Arrange
        // Pre-condición: No hay donantes
        assertEquals(0, repositorioPersonas.obtenerTodas().size());

        // Act
        importador.importarPersonasDesdeCSV("donantes_solo_persona_humana.csv");

        // Assert
        assertEquals(6, repositorioPersonas.obtenerTodas().size());

        // El donante existe
        Optional<Persona> donanteOpt = repositorioPersonas.buscarPorEmail("ananavarro3658@yahoo.com");
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

        // Verifica que se envió la notificación de bienvenida
        assertTrue(outContent.toString().contains("¡Hola Ana! Te damos la bienvenida a dona-track."));
    }

    @Test
    void importarPersonasDesdeCSV_CreaNuevaPersonaJuridica_SiNoExiste() {
        // Arrange
        // Pre-condición: No hay donantes
        assertEquals(0, repositorioPersonas.obtenerTodas().size());

        // Act
        importador.importarPersonasDesdeCSV("donantes_solo_persona_juridica.csv");

        // Assert
        assertEquals(6, repositorioPersonas.obtenerTodas().size());

        // El donante existe
        Optional<Persona> donanteOpt = repositorioPersonas.buscarPorEmail("santafeindustrial8180@yahoo.com");
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
    void importarPersonasDesdeCSV_ActualizaPersona_SiYaExiste(){
        // Arrange: Pre-cargamos una persona humana en la aplicación
        List<Contacto> contactosAna = List.of(new Contacto(TipoContacto.EMAIL, "ananavarro3658@yahoo.com"));
        Usuario usuarioAna = new Usuario("ana_viejo", "passVieja");
        PersonaHumana donanteExistente = new PersonaHumana(
            "Nombre_viejo", "Apellido_viejo", 1, TipoDoc.DNI, "1", Genero.FEMENINO, "Av. Siempre Viva 742",
            contactosAna, contactosAna.get(0), usuarioAna
        );

        repositorioPersonas.agregar(donanteExistente);

        // Pre-condición: El donante existe con su nombre original
        assertEquals(1, repositorioPersonas.obtenerTodas().size());

        // Act - El CSV contiene el mismo donante pero con datos actualizados
        importador.importarPersonasDesdeCSV("donantes_solo_persona_humana.csv");

        // Assert
        assertEquals(6, repositorioPersonas.obtenerTodas().size());

        // El donante debe seguir existiendo
        Optional<Persona> donanteOpt = repositorioPersonas.buscarPorEmail("ananavarro3658@yahoo.com");
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
