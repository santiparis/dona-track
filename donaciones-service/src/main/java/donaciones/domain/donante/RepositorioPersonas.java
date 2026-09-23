package donaciones.domain.donante;

import donaciones.persistence.JpaContext;
import javax.persistence.EntityManager;
import java.util.List;
import java.util.Optional;

/** Repositorio JPA de personas; no mantiene estado en memoria. */
public class RepositorioPersonas {
    private final EntityManager entityManager;
    public RepositorioPersonas() { this(JpaContext.entityManager()); }
    public RepositorioPersonas(EntityManager entityManager) { this.entityManager = entityManager; }
    public Optional<Persona> buscarPorId(Long id) { return Optional.ofNullable(entityManager.find(Persona.class, id)); }
    public Optional<Persona> buscarPorEmail(String email) { return obtenerTodas().stream().filter(p -> email.equals(p.getEmail())).findFirst(); }
    public Optional<Persona> buscarPorDocumento(String documento) { return entityManager.createQuery("from Persona p where p.documento = :documento", Persona.class).setParameter("documento", documento).getResultStream().findFirst(); }
    public void agregar(Persona persona) { JpaContext.inTransaction(em -> { if (persona.getId() == null) em.persist(persona); else em.merge(persona); }); }
    public void eliminarPorId(Long id) { JpaContext.inTransaction(em -> { Persona p = em.find(Persona.class, id); if (p != null) em.remove(p); }); }
    public void eliminarPorDocumento(String documento) { buscarPorDocumento(documento).ifPresent(p -> eliminarPorId(p.getId())); }
    public List<Persona> obtenerTodas() { return entityManager.createQuery("from Persona", Persona.class).getResultList(); }
}
