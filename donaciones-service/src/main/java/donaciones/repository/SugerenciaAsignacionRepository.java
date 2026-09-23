package donaciones.repository;

import donaciones.domain.algoritmos.SugerenciaAsignacion;
import donaciones.persistence.JpaContext;
import javax.persistence.EntityManager;
import java.util.List;
import java.util.Optional;

public class SugerenciaAsignacionRepository {
  private final EntityManager entityManager;
  public SugerenciaAsignacionRepository() { this(JpaContext.entityManager()); }
  public SugerenciaAsignacionRepository(EntityManager entityManager) { this.entityManager = entityManager; }
  public void guardar(SugerenciaAsignacion sugerencia) { JpaContext.inTransaction(em -> { if (em.find(SugerenciaAsignacion.class, sugerencia.getId()) == null) em.persist(sugerencia); else em.merge(sugerencia); }); }
  public List<SugerenciaAsignacion> obtenerTodas() { return entityManager.createQuery("from SugerenciaAsignacion", SugerenciaAsignacion.class).getResultList(); }
  public void limpiar() { JpaContext.inTransaction(em -> { em.createQuery("delete from SugerenciaAsignacion").executeUpdate(); }); }
  public void eliminar(SugerenciaAsignacion sugerencia) { JpaContext.inTransaction(em -> { SugerenciaAsignacion managed = em.find(SugerenciaAsignacion.class, sugerencia.getId()); if (managed != null) em.remove(managed); }); }
  public Optional<SugerenciaAsignacion> buscarPorID(Long id) { return Optional.ofNullable(entityManager.find(SugerenciaAsignacion.class, id)); }
}
