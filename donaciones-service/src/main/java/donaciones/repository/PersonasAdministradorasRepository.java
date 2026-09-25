package donaciones.repository;

import donaciones.domain.PersonaAdministradora;
import donaciones.persistence.JpaContext;
import donaciones.persistence.JpaRepository;
import javax.persistence.EntityManager;
import java.util.List;

/** Repositorio JPA de administradoras. */
public class PersonasAdministradorasRepository extends JpaRepository {
    public PersonasAdministradorasRepository() { this(JpaContext.INSTANCE.entityManager()); }
    public PersonasAdministradorasRepository(EntityManager entityManager) { super(entityManager); }
    public List<PersonaAdministradora> obtenerTodos() { return entityManager().createQuery("from PersonaAdministradora", PersonaAdministradora.class).getResultList(); }
    public void guardar(PersonaAdministradora admin) { enTransaccion(() -> { if (admin.getId() == null) entityManager().persist(admin); else entityManager().merge(admin); }); }
}
