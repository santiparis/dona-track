package db;

import donaciones.CrearAdministradora;
import donaciones.domain.PersonaAdministradora;
import donaciones.domain.donante.Rol;
import donaciones.domain.donante.Usuario;
import donaciones.repository.PersonasAdministradorasRepository;
import donaciones.repository.UsuarioRepository;
import io.github.flbulgarelli.jpa.extras.test.SimplePersistenceTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CrearAdministradoraTest implements SimplePersistenceTest {
  private final UsuarioRepository usuarios = new UsuarioRepository();
  private final PersonasAdministradorasRepository administradoras = new PersonasAdministradorasRepository();

  @Test
  void persisteLaCuentaYSuTitularConElRolAdministrador() {
    PersonaAdministradora creada = CrearAdministradora.crear("  ADMIN.PRUEBA  ", "clave-del-admin", "  Ana  ");
    entityManager().flush();
    entityManager().clear();

    Usuario cuenta = usuarios.buscarPorNombreUsuario("admin.prueba").orElseThrow();
    PersonaAdministradora titular = administradoras.buscarPorUsuario(cuenta.getId()).orElseThrow();
    assertEquals(creada.getId(), titular.getId());
    assertEquals("Ana", titular.getNombre());
    assertEquals(Rol.ADMINISTRADORA, cuenta.getRol());
    assertTrue(cuenta.validarPassword("clave-del-admin"));
    assertFalse(cuenta.validarPassword("otra-clave"));
  }

  @Test
  void noReemplazaUnaAdministradoraExistenteNiSuPassword() {
    PersonaAdministradora original = CrearAdministradora.crear("admin.existe", "clave-original", "Original");

    assertThrows(IllegalArgumentException.class,
        () -> CrearAdministradora.crear(" ADMIN.EXISTE ", "clave-nueva", "Otra persona"));
    entityManager().flush();
    entityManager().clear();

    Usuario cuenta = usuarios.buscarPorNombreUsuario("admin.existe").orElseThrow();
    assertTrue(cuenta.validarPassword("clave-original"));
    assertFalse(cuenta.validarPassword("clave-nueva"));
    PersonaAdministradora titular = administradoras.buscarPorUsuario(cuenta.getId()).orElseThrow();
    assertEquals(original.getId(), titular.getId());
    assertEquals("Original", titular.getNombre());
  }

  @Test
  void noConvierteOtraCuentaEnAdministradora() {
    Usuario donante = new Usuario("donante.existe", "clave-donante", Rol.DONANTE);
    persist(donante);

    assertThrows(IllegalArgumentException.class,
        () -> CrearAdministradora.crear("donante.existe", "clave-nueva", "Admin"));
    assertEquals(Rol.DONANTE, usuarios.buscarPorNombreUsuario("donante.existe").orElseThrow().getRol());
    assertTrue(administradoras.buscarPorUsuario(donante.getId()).isEmpty());
    assertTrue(donante.validarPassword("clave-donante"));
  }

  @Test
  void rechazaConfiguracionIncompletaSinCrearUnaCuenta() {
    assertThrows(IllegalArgumentException.class, () -> CrearAdministradora.crear(null, "clave", "Ana"));
    assertThrows(IllegalArgumentException.class, () -> CrearAdministradora.crear(" ", "clave", "Ana"));
    assertThrows(IllegalArgumentException.class, () -> CrearAdministradora.crear("incompleta", null, "Ana"));
    assertThrows(IllegalArgumentException.class, () -> CrearAdministradora.crear("incompleta", " ", "Ana"));
    assertThrows(IllegalArgumentException.class, () -> CrearAdministradora.crear("incompleta", "clave", null));
    assertThrows(IllegalArgumentException.class, () -> CrearAdministradora.crear("incompleta", "clave", " "));
    assertTrue(usuarios.buscarPorNombreUsuario("incompleta").isEmpty());
  }
}
