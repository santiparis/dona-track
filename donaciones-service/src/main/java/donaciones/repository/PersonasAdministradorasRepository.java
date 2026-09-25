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
    public List<PersonaAdministradora> obtenerTodos() {
        List<PersonaAdministradora> administradoras = entityManager()
            .createQuery("from PersonaAdministradora", PersonaAdministradora.class).getResultList();
        administradoras.forEach(this::reconstruirContactos);
        return administradoras;
    }
    public void guardar(PersonaAdministradora admin) { enTransaccion(() -> { if (admin.getId() == null) entityManager().persist(admin); else entityManager().merge(admin); }); }

    private void reconstruirContactos(PersonaAdministradora administradora) {
        if (administradora != null && administradora.getId() != null) {
            List<donaciones.domain.notificacion.Contacto> contactos = new ContactoRepository(entityManager())
                .buscarPorNotificable(administradora.getId(), "ADMIN");
            if (!contactos.isEmpty()) administradora.reconstruirContactos(contactos);
        }
    }
}
