package donaciones.repository;

import donaciones.domain.notificacion.Notificacion;
import donaciones.db.TransaccionPorRequest;
import io.github.flbulgarelli.jpa.extras.simple.WithSimplePersistenceUnit;

import java.util.List;
import java.util.Optional;

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
        return createQuery("from Notificacion order by fecha", Notificacion.class).getResultList();
    }

    public List<Notificacion> buscarPorReceptor(String tipo, Long id) {
        TransaccionPorRequest.iniciarSiEsNecesario();
        return createQuery("from Notificacion n where n.tipoReceptor = :tipo and n.receptorId = :id order by n.fecha desc", Notificacion.class)
                .setParameter("tipo", tipo)
                .setParameter("id", id)
                .getResultList();
    }

    public Optional<Notificacion> buscarDelReceptor(Long notificacionId, String tipo, Long id) {
        TransaccionPorRequest.iniciarSiEsNecesario();
        return createQuery("from Notificacion n where n.id = :notificacionId and n.tipoReceptor = :tipo and n.receptorId = :id", Notificacion.class)
                .setParameter("notificacionId", notificacionId)
                .setParameter("tipo", tipo)
                .setParameter("id", id)
                .getResultStream()
                .findFirst();
    }

    // El usuario puede ser de una persona donante, una entidad o una administradora
    public Long contarNoLeidasDelUsuario(Long usuarioId) {
        TransaccionPorRequest.iniciarSiEsNecesario();
        return createQuery("select count(n) from Notificacion n where n.leida = false and ("
                + "(n.tipoReceptor = 'PERSONA' and n.receptorId in (select p.id from Persona p where p.usuario.id = :usuarioId))"
                + " or (n.tipoReceptor = 'ENTIDAD' and n.receptorId in (select e.id from EntidadBeneficiaria e where e.usuario.id = :usuarioId))"
                + " or (n.tipoReceptor = 'ADMINISTRADORA' and n.receptorId in (select a.id from PersonaAdministradora a where a.usuario.id = :usuarioId)))", Long.class)
                .setParameter("usuarioId", usuarioId)
                .getSingleResult();
    }
}
