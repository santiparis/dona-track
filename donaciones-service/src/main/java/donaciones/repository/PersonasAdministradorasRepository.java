package donaciones.repository;

import donaciones.domain.PersonaAdministradora;
import donaciones.db.TransaccionPorRequest;
import io.github.flbulgarelli.jpa.extras.simple.WithSimplePersistenceUnit;

import java.util.List;

/** Repositorio JPA de administradoras. */
public class PersonasAdministradorasRepository implements WithSimplePersistenceUnit {
    public List<PersonaAdministradora> obtenerTodos() {
        return createQuery("from PersonaAdministradora", PersonaAdministradora.class).getResultList();
    }

    public void guardar(PersonaAdministradora admin) {
        TransaccionPorRequest.iniciarSiEsNecesario();
        if (admin.getId() == null) persist(admin);
    }
}
