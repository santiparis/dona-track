package donaciones.domain.donante;

import donaciones.persistence.JpaContext;
import donaciones.persistence.JpaRepository;
import javax.persistence.EntityManager;
import java.util.List;
import java.util.Optional;

/** Repositorio JPA de personas; no mantiene estado en memoria. */
public class RepositorioPersonas extends JpaRepository {
    public RepositorioPersonas() { this(JpaContext.INSTANCE.entityManager()); }
    public RepositorioPersonas(EntityManager entityManager) { super(entityManager); }
    public Optional<Persona> buscarPorId(Long id) { return Optional.ofNullable(entityManager().find(Persona.class, id)); }
    public Optional<Persona> buscarPorEmail(String email) { return obtenerTodas().stream().filter(p -> email.equals(p.getEmail())).findFirst(); }
    public Optional<Persona> buscarPorDocumento(String documento) { return entityManager().createQuery("from Persona p where p.documento = :documento", Persona.class).setParameter("documento", documento).getResultStream().findFirst(); }
    public void agregar(Persona persona) { enTransaccion(() -> { if (persona.getId() == null) entityManager().persist(persona); else entityManager().merge(persona); }); }
    public void eliminarPorId(Long id) { enTransaccion(() -> { Persona p = entityManager().find(Persona.class, id); if (p != null) entityManager().remove(p); }); }
    public void eliminarPorDocumento(String documento) { buscarPorDocumento(documento).ifPresent(p -> eliminarPorId(p.getId())); }
    public List<Persona> obtenerTodas() { return entityManager().createQuery("from Persona", Persona.class).getResultList(); }
}
