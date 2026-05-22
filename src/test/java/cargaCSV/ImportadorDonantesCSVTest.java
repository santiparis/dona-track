package cargaCSV;

import donante.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class ImportadorDonantesCSVTest {

    private static List<Persona> personas;

    @BeforeAll
    static void setUp() throws IOException {
        // Se ejecuta una sola vez para toda la clase, ideal para archivos grandes.
        String pathCsv = "donantes_import_20000_UTF8_BOM.csv";
        ImportadorDonantesCSV importador = new ImportadorDonantesCSV(pathCsv);
        personas = importador.procesar();
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
        ImportadorDonantesCSV importadorInvalido = new ImportadorDonantesCSV(pathCsvInvalido);

        // Verificamos que al procesar el archivo inválido, se lanza nuestra excepción custom.
        assertThrows(MapeoCsvEnDonanteException.class, () -> {
            importadorInvalido.procesar();
        });
    }
}
