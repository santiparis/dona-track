package donaciones.repository;

import donaciones.domain.PersonaAdministradora;
import donaciones.persistence.JpaContext;
import javax.persistence.EntityManager;
import java.util.List;

/** Repositorio JPA de administradoras. */
public class PersonasAdministradorasRepository {
    private final EntityManager entityManager;
    public PersonasAdministradorasRepository() { this(JpaContext.entityManager()); }
    public PersonasAdministradorasRepository(EntityManager entityManager) { this.entityManager = entityManager; }
    public List<PersonaAdministradora> obtenerTodos() { return entityManager.createQuery("from PersonaAdministradora", PersonaAdministradora.class).getResultList(); }
    public void guardar(PersonaAdministradora admin) { JpaContext.inTransaction(em -> { if (admin.getId() == null) em.persist(admin); else em.merge(admin); }); }
}
