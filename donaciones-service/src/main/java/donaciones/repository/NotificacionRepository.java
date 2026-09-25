package donaciones.repository;

import donaciones.domain.notificacion.Notificacion;
import donaciones.persistence.JpaContext;
import donaciones.persistence.JpaRepository;

import javax.persistence.EntityManager;
import java.util.List;

public class NotificacionRepository extends JpaRepository {
    public NotificacionRepository() { this(JpaContext.INSTANCE.entityManager()); }
    public NotificacionRepository(EntityManager entityManager) { super(entityManager); }

    public void guardar(Notificacion notificacion) {
        enTransaccion(() -> entityManager().persist(notificacion));
    }

    public List<Notificacion> obtenerTodas() {
        return entityManager().createQuery("from Notificacion order by fecha", Notificacion.class).getResultList();
    }
}