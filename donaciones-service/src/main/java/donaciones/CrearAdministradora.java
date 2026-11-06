package donaciones;

import donaciones.db.TransaccionPorRequest;
import donaciones.domain.PersonaAdministradora;
import donaciones.domain.donante.Rol;
import donaciones.domain.donante.Usuario;
import donaciones.repository.PersonasAdministradorasRepository;
import donaciones.repository.UsuarioRepository;

import java.util.List;
import java.util.Locale;

/** Alta manual de administración; este rol nunca se ofrece en el registro público. */
public class CrearAdministradora {
  public static void main(String[] args) {
    TransaccionPorRequest transaccion = new TransaccionPorRequest();
    try {
      transaccion.withTransaction(() -> crear(
          System.getenv("ADMIN_USUARIO"),
          System.getenv("ADMIN_PASSWORD"),
          System.getenv("ADMIN_NOMBRE")));
      System.out.println("Cuenta administradora creada.");
    } finally {
      if (transaccion.perThreadEntityManagerAccess().isAttached()) {
        transaccion.perThreadEntityManagerAccess().dispose();
      }
    }
  }

  /** El llamador abre la transacción para guardar cuenta y titular juntos. */
  public static PersonaAdministradora crear(String nombreUsuario, String password, String nombre) {
    if (nombreUsuario == null || nombreUsuario.isBlank()) {
      throw new IllegalArgumentException("Falta ADMIN_USUARIO.");
    }
    if (password == null || password.isBlank()) {
      throw new IllegalArgumentException("Falta ADMIN_PASSWORD.");
    }
    if (nombre == null || nombre.isBlank()) {
      throw new IllegalArgumentException("Falta ADMIN_NOMBRE.");
    }

    String usuarioNormalizado = nombreUsuario.trim().toLowerCase(Locale.ROOT);
    if (new UsuarioRepository().buscarPorNombreUsuario(usuarioNormalizado).isPresent()) {
      throw new IllegalArgumentException("Ese usuario ya existe. No se modificó su cuenta.");
    }

    Usuario usuario = new Usuario(usuarioNormalizado, password, Rol.ADMINISTRADORA);
    PersonaAdministradora administradora = new PersonaAdministradora(nombre.trim(), List.of(), null, usuario);
    new PersonasAdministradorasRepository().guardar(administradora);
    return administradora;
  }
}
