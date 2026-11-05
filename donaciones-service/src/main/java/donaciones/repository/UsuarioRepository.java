package donaciones.repository;

import donaciones.db.TransaccionPorRequest;
import donaciones.domain.donante.Usuario;
import io.github.flbulgarelli.jpa.extras.simple.WithSimplePersistenceUnit;
import java.util.Optional;

public class UsuarioRepository implements WithSimplePersistenceUnit {
    public Optional<Usuario> buscarPorNombreUsuario(String nombreUsuario) {
        TransaccionPorRequest.iniciarSiEsNecesario();
        return createQuery("from Usuario u where u.nombreUsuario = :nombreUsuario", Usuario.class)
                .setParameter("nombreUsuario", nombreUsuario)
                .getResultStream()
                .findFirst();
    }

    public Optional<Usuario> buscarPorId(Long id) {
        TransaccionPorRequest.iniciarSiEsNecesario();
        return Optional.ofNullable(find(Usuario.class, id));
    }
}

