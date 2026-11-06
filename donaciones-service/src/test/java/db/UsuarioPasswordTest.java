package db;

import com.fasterxml.jackson.databind.ObjectMapper;
import donaciones.domain.donante.Rol;
import donaciones.domain.donante.Usuario;
import io.github.flbulgarelli.jpa.extras.test.SimplePersistenceTest;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class UsuarioPasswordTest implements SimplePersistenceTest {
    @Test
    void guardaHashesDistintosYVerificaDespuesDeRecargar() throws Exception {
        String clave = "contraseña-segura-123";
        Usuario primero = new Usuario("primero", clave, Rol.DONANTE);
        Usuario segundo = new Usuario("segundo", clave, Rol.ENTIDAD);
        entityManager().persist(primero);
        entityManager().persist(segundo);
        entityManager().flush();
        Long id = primero.getId();
        String hash1 = hashGuardado(id);
        String hash2 = hashGuardado(segundo.getId());
        assertNotEquals(clave, hash1);
        assertNotEquals(hash1, hash2);
        entityManager().clear();
        Usuario recuperado = entityManager().find(Usuario.class, id);
        assertTrue(recuperado.validarPassword(clave));
        assertFalse(recuperado.validarPassword("incorrecta"));
        assertFalse(recuperado.validarPassword(null));
        assertFalse(recuperado.validarPassword(""));
        String json = new ObjectMapper().writeValueAsString(recuperado);
        assertFalse(json.contains(clave));
        assertFalse(json.contains(hash1));
        assertFalse(json.contains("password"));
    }

    @Test
    void rechazaCredencialesAntiguasEnTextoPlanoYHashesInvalidos() {
        Usuario usuario = new Usuario("antiguo", "secreto", Rol.DONANTE);
        entityManager().persist(usuario);
        entityManager().flush();
        Long id = usuario.getId();
        for (String valor : new String[]{"secreto", "pbkdf2-sha256$600000$invalid$invalid"}) {
            entityManager().createNativeQuery("update usuario set contrasenia = :valor where usuario_id = :id")
                    .setParameter("valor", valor).setParameter("id", id).executeUpdate();
            entityManager().clear();
            assertFalse(entityManager().find(Usuario.class, id).validarPassword("secreto"));
        }
    }

    @Test
    void noPermiteCrearUsuariosSinContrasenia() {
        assertThrows(IllegalArgumentException.class, () -> new Usuario("a", null, Rol.DONANTE));
        assertThrows(IllegalArgumentException.class, () -> new Usuario("a", "  ", Rol.DONANTE));
    }

    private String hashGuardado(Long id) {
        return (String) entityManager().createNativeQuery("select contrasenia from usuario where usuario_id = :id")
                .setParameter("id", id).getSingleResult();
    }
}

