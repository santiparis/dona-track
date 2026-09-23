package donaciones.repository;

import donaciones.domain.EntidadBeneficiaria;
import donaciones.persistence.JpaContext;
import javax.persistence.EntityManager;
import java.util.List;
import java.util.Optional;

public class EntidadBeneficiariaRepository {
  private final EntityManager entityManager;
  public EntidadBeneficiariaRepository() { this(JpaContext.entityManager()); }
  public EntidadBeneficiariaRepository(EntityManager entityManager) { this.entityManager = entityManager; }
  public List<EntidadBeneficiaria> obtenerTodas() { return entityManager.createQuery("from EntidadBeneficiaria", EntidadBeneficiaria.class).getResultList(); }
  public Optional<EntidadBeneficiaria> buscarPorId(Long id) { return Optional.ofNullable(entityManager.find(EntidadBeneficiaria.class, id)); }
  public Optional<EntidadBeneficiaria> obtenerPorId(Long id) { return buscarPorId(id); }
  public void guardar(EntidadBeneficiaria entidad) { JpaContext.inTransaction(em -> { if (entidad.getId() == null) em.persist(entidad); else em.merge(entidad); }); }
  public void eliminarPorId(Long id) { JpaContext.inTransaction(em -> { EntidadBeneficiaria e = em.find(EntidadBeneficiaria.class, id); if (e != null) em.remove(e); }); }
}
