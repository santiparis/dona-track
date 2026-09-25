package donaciones.repository;

import donaciones.domain.algoritmos.SugerenciaAsignacion;
import donaciones.persistence.JpaContext;
import donaciones.persistence.JpaRepository;
import javax.persistence.EntityManager;
import java.util.List;
import java.util.Optional;

public class SugerenciaAsignacionRepository extends JpaRepository {
  public SugerenciaAsignacionRepository() { this(JpaContext.INSTANCE.entityManager()); }
  public SugerenciaAsignacionRepository(EntityManager entityManager) { super(entityManager); }
  public void guardar(SugerenciaAsignacion sugerencia) { enTransaccion(() -> { if (entityManager().find(SugerenciaAsignacion.class, sugerencia.getId()) == null) entityManager().persist(sugerencia); else entityManager().merge(sugerencia); }); }
  public List<SugerenciaAsignacion> obtenerTodas() { return entityManager().createQuery("from SugerenciaAsignacion", SugerenciaAsignacion.class).getResultList(); }
  public void limpiar() { enTransaccion(() -> entityManager().createQuery("delete from SugerenciaAsignacion").executeUpdate()); }
  public void eliminar(SugerenciaAsignacion sugerencia) { enTransaccion(() -> { SugerenciaAsignacion managed = entityManager().find(SugerenciaAsignacion.class, sugerencia.getId()); if (managed != null) entityManager().remove(managed); }); }
  public Optional<SugerenciaAsignacion> buscarPorID(Long id) { return Optional.ofNullable(entityManager().find(SugerenciaAsignacion.class, id)); }
}
