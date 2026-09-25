package donaciones.repository;

import donaciones.domain.EntidadBeneficiaria;
import donaciones.persistence.JpaContext;
import donaciones.persistence.JpaRepository;
import javax.persistence.EntityManager;
import java.util.List;
import java.util.Optional;

public class EntidadBeneficiariaRepository extends JpaRepository {
  public EntidadBeneficiariaRepository() { this(JpaContext.INSTANCE.entityManager()); }
  public EntidadBeneficiariaRepository(EntityManager entityManager) { super(entityManager); }
  public List<EntidadBeneficiaria> obtenerTodas() { return entityManager().createQuery("from EntidadBeneficiaria", EntidadBeneficiaria.class).getResultList(); }
  public Optional<EntidadBeneficiaria> buscarPorId(Long id) { return Optional.ofNullable(entityManager().find(EntidadBeneficiaria.class, id)); }
  public Optional<EntidadBeneficiaria> obtenerPorId(Long id) { return buscarPorId(id); }
  public void guardar(EntidadBeneficiaria entidad) { enTransaccion(() -> { if (entidad.getId() == null) entityManager().persist(entidad); else entityManager().merge(entidad); }); }
  public void eliminarPorId(Long id) { enTransaccion(() -> { EntidadBeneficiaria e = entityManager().find(EntidadBeneficiaria.class, id); if (e != null) entityManager().remove(e); }); }
}
