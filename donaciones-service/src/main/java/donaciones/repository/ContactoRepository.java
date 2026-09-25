package donaciones.repository;

import donaciones.domain.notificacion.Contacto;
import donaciones.persistence.JpaContext;
import donaciones.persistence.JpaRepository;

import javax.persistence.EntityManager;
import java.util.List;

public class ContactoRepository extends JpaRepository {
    public ContactoRepository() { this(JpaContext.INSTANCE.entityManager()); }
    public ContactoRepository(EntityManager entityManager) { super(entityManager); }

    public List<Contacto> buscarPorNotificable(Long notificableId, String tipoNotificable) {
        return entityManager().createQuery(
                "from Contacto c where c.notificableId = :id and c.tipoNotificable = :tipo",
                Contacto.class)
            .setParameter("id", notificableId)
            .setParameter("tipo", tipoNotificable)
            .getResultList();
    }
}