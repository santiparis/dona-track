package cargaCSV;

import donaciones.domain.cargaCSV.ImportadorPersonas;
import donaciones.domain.cargaCSV.MapeoCsvEnPersonaException;
import donaciones.domain.donante.Genero;
import donaciones.domain.donante.Persona;
import donaciones.domain.donante.PersonaHumana;
import donaciones.domain.donante.RepositorioPersonas;
import donaciones.domain.donante.TipoDoc;
import donaciones.domain.donante.Usuario;
import donaciones.domain.notificacion.Contacto;
import donaciones.domain.notificacion.ContactoPorEmail;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import io.github.flbulgarelli.jpa.extras.test.SimplePersistenceTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ImportadorPersonasTest implements SimplePersistenceTest {
    private RepositorioPersonas repositorioPersonas;
    private ImportadorPersonas importador;

    @BeforeEach
    void setUp() {
        repositorioPersonas = new RepositorioPersonas(entityManager());
        importador = new ImportadorPersonas(repositorioPersonas);
    }

    @Test
    void procesarArchivoInvalidoLanzaExcepcionDeMapeo() {
        assertThrows(MapeoCsvEnPersonaException.class,
                () -> importador.importarPersonasDesdeCSV("donantes_invalidos.csv"));
    }

    @Test
    void importarPersonasHumanasLasPersisteEnHsqldb() {
        importador.importarPersonasDesdeCSV("donantes_solo_persona_humana.csv");

        assertEquals(6, repositorioPersonas.obtenerTodas().size());
        assertTrue(repositorioPersonas.buscarPorDocumento("28456905").isPresent());
    }

    @Test
    void importarPersonasJuridicasLasPersisteEnHsqldb() {
        importador.importarPersonasDesdeCSV("donantes_solo_persona_juridica.csv");

        assertEquals(6, repositorioPersonas.obtenerTodas().size());
        assertTrue(repositorioPersonas.buscarPorDocumento("30-52235350-3").isPresent());
    }

    @Test
    void importarActualizaLaPersonaExistenteEnLugarDeDuplicarla() {
        Contacto contacto = new ContactoPorEmail("ananavarro3658@yahoo.com");
        PersonaHumana existente = new PersonaHumana(
                "Nombre viejo", "Apellido viejo", 1, TipoDoc.DNI, "1", Genero.FEMENINO,
                "Av. Siempre Viva 742", List.of(contacto), contacto, new Usuario("ana_viejo", "passVieja"));
        repositorioPersonas.agregar(existente);

        importador.importarPersonasDesdeCSV("donantes_solo_persona_humana.csv");

        List<Persona> personas = repositorioPersonas.obtenerTodas();
        assertEquals(6, personas.size());
        assertEquals("Ana", personas.stream()
                .filter(persona -> persona.getId().equals(existente.getId()))
                .findFirst().orElseThrow().getNombre());
    }
}
