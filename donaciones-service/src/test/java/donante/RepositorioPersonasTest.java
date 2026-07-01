package donante;

import donaciones.domain.donante.Contacto;
import donaciones.domain.donante.Genero;
import donaciones.domain.donante.Persona;
import donaciones.domain.donante.PersonaHumana;
import donaciones.domain.donante.PersonaJuridica;
import donaciones.domain.donante.RazonSocial;
import donaciones.domain.donante.RepositorioPersonas;
import donaciones.domain.donante.TipoDoc;
import donaciones.domain.donante.Usuario;
import donaciones.domain.notificacion.NotificacionPorEmail;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class RepositorioPersonasTest {

    private RepositorioPersonas repositorioPersonas;
    private PersonaHumana donanteHumano;
    private PersonaJuridica donanteJuridico;
    NotificacionPorEmail estrategiaEmail = new NotificacionPorEmail();

    @BeforeEach
    void setUp() {
        repositorioPersonas = new RepositorioPersonas();
        List<Contacto> contactos = List.of(new Contacto(estrategiaEmail, "test@test.com"));
        Usuario usuario = new Usuario("user", "pass");
        donanteHumano = new PersonaHumana("Nombre", "Apellido", 30, TipoDoc.DNI, "12345678", Genero.MASCULINO, "Calle Falsa 123", contactos, contactos.get(0), usuario);
        donanteJuridico = new PersonaJuridica(TipoDoc.CUIT, "30-12345678-9", "Empresa S.A.", RazonSocial.EMPRESA, "Tecnología", Collections.emptyList(), contactos, contactos.get(0), usuario);
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
        List<Contacto> contactos = List.of(new Contacto(estrategiaEmail, "test@test.com"));
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
        List<Contacto> contactos = List.of(new Contacto(estrategiaEmail, "test@test.com"));
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
        assertTrue(repositorioPersonas.buscarPorDocumento("12345678").isEmpty());
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
        assertTrue(repositorioPersonas.buscarPorDocumento("30-12345678-9").isEmpty());
    }
}