package donaciones.repository;

import donaciones.domain.notificacion.Notificacion;
import donaciones.db.TransaccionPorRequest;
import io.github.flbulgarelli.jpa.extras.simple.WithSimplePersistenceUnit;

import java.util.List;

public class NotificacionRepository implements WithSimplePersistenceUnit {
    public void guardar(Notificacion notificacion) {
        TransaccionPorRequest.iniciarSiEsNecesario();
        boolean transactionActiva = getTransaction().isActive();
        if (transactionActiva) {
            persist(notificacion);
        } else {
            try {
                withTransaction(() -> persist(notificacion));
            } finally {
                perThreadEntityManagerAccess().dispose();
            }
        }
    }

    public List<Notificacion> obtenerTodas() {
        TransaccionPorRequest.iniciarSiEsNecesario();
        return createQuery("from Notificacion order by fecha", Notificacion.class).getResultList();
    }
}