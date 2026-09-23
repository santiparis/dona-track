package donaciones.repository;

import donaciones.domain.Donacion;
import donaciones.persistence.JpaContext;
import javax.persistence.EntityManager;
import java.util.List;
import java.util.Optional;

public class DonacionRepository {
  private final EntityManager entityManager;
  public DonacionRepository() { this(JpaContext.entityManager()); }
  public DonacionRepository(EntityManager entityManager) { this.entityManager = entityManager; }
  public void guardar(Donacion donacion) { JpaContext.inTransaction(em -> { if (donacion.getId() == null) em.persist(donacion); else em.merge(donacion); }); }
  public List<Donacion> obtenerTodas() { return entityManager.createQuery("from Donacion", Donacion.class).getResultList(); }
  public Optional<Donacion> buscarPorId(Long id) { return Optional.ofNullable(entityManager.find(Donacion.class, id)); }
  public void borrarPorId(Long id) { JpaContext.inTransaction(em -> { Donacion d = em.find(Donacion.class, id); if (d != null) em.remove(d); }); }
}
