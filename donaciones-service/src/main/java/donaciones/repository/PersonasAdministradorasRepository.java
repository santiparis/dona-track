package donaciones.repository;

import donaciones.domain.PersonaAdministradora;
import donaciones.db.TransaccionPorRequest;
import io.github.flbulgarelli.jpa.extras.simple.WithSimplePersistenceUnit;

import java.util.List;
import java.util.Optional;

/** Repositorio JPA de administradoras. */
public class PersonasAdministradorasRepository implements WithSimplePersistenceUnit {
    public Optional<PersonaAdministradora> buscarPorUsuario(Long usuarioId) {
        TransaccionPorRequest.iniciarSiEsNecesario();
        return createQuery("from PersonaAdministradora p where p.usuario.id = :usuarioId", PersonaAdministradora.class)
                .setParameter("usuarioId", usuarioId)
                .getResultStream()
                .findFirst();
    }


    public List<PersonaAdministradora> obtenerTodos() {
        return createQuery("from PersonaAdministradora", PersonaAdministradora.class).getResultList();
    }

    public void guardar(PersonaAdministradora admin) {
        TransaccionPorRequest.iniciarSiEsNecesario();
        if (admin.getId() == null) persist(admin);
    }
}
