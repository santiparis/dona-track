package db;

import donaciones.domain.EntidadBeneficiaria;
import donaciones.domain.PersonaAdministradora;
import donaciones.domain.donante.*;
import donaciones.domain.notificacion.ContactoPorEmail;
import donaciones.repository.*;
import io.github.flbulgarelli.jpa.extras.test.SimplePersistenceTest;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class UsuarioRepositoryTest implements SimplePersistenceTest {
    private final UsuarioRepository usuarios = new UsuarioRepository();
    private final RepositorioPersonas personas = new RepositorioPersonas();
    private final EntidadBeneficiariaRepository entidades = new EntidadBeneficiariaRepository();
    private final PersonasAdministradorasRepository administradoras = new PersonasAdministradorasRepository();

    @Test
    void encuentraCadaCuentaYSuTitularSinConfundirLosRoles() {
        Usuario donante = new Usuario("donante@example.org", "secreto", Rol.DONANTE);
        Usuario entidad = new Usuario("entidad@example.org", "secreto", Rol.ENTIDAD);
        Usuario admin = new Usuario("admin@example.org", "secreto", Rol.ADMINISTRADORA);
        ContactoPorEmail contacto = new ContactoPorEmail("donante@example.org");
        PersonaHumana persona = new PersonaHumana("Ana", "Perez", 30, TipoDoc.DNI, "12345678",
                Genero.FEMENINO, "Calle 1", List.of(contacto), contacto, donante);
        EntidadBeneficiaria beneficiaria = new EntidadBeneficiaria("Comedor", "Calle 2", "111",
                List.of(), entidad);
        PersonaAdministradora administradora = new PersonaAdministradora("Admin", List.of(), null, admin);
        personas.agregar(persona);
        entidades.guardar(beneficiaria);
        administradoras.guardar(administradora);
        entityManager().flush();
        entityManager().clear();

        for (Usuario esperado : List.of(donante, entidad, admin)) {
            Usuario encontrado = usuarios.buscarPorNombreUsuario(esperado.getNombreUsuario()).orElseThrow();
            assertEquals(esperado.getId(), encontrado.getId());
            assertEquals(esperado.getRol(), encontrado.getRol());
            assertEquals(esperado.getNombreUsuario(), usuarios.buscarPorId(esperado.getId()).orElseThrow().getNombreUsuario());
        }
        assertEquals(persona.getId(), personas.buscarPorUsuario(donante.getId()).orElseThrow().getId());
        assertEquals(beneficiaria.getId(), entidades.buscarPorUsuario(entidad.getId()).orElseThrow().getId());
        assertEquals(administradora.getId(), administradoras.buscarPorUsuario(admin.getId()).orElseThrow().getId());
        assertTrue(personas.buscarPorUsuario(entidad.getId()).isEmpty());
        assertTrue(entidades.buscarPorUsuario(admin.getId()).isEmpty());
        assertTrue(administradoras.buscarPorUsuario(donante.getId()).isEmpty());
    }

    @Test
    void devuelveVacioSiNoExisteLaCuentaOElTitular() {
        assertTrue(usuarios.buscarPorNombreUsuario("inexistente").isEmpty());
        assertTrue(usuarios.buscarPorId(-1L).isEmpty());
        assertTrue(personas.buscarPorUsuario(-1L).isEmpty());
        assertTrue(entidades.buscarPorUsuario(-1L).isEmpty());
        assertTrue(administradoras.buscarPorUsuario(-1L).isEmpty());
    }
}
