package donaciones.repository;

import donaciones.domain.Donacion;
import donaciones.persistence.JpaContext;
import donaciones.persistence.JpaRepository;
import javax.persistence.EntityManager;
import java.util.List;
import java.util.Optional;

public class DonacionRepository extends JpaRepository {
  public DonacionRepository() { this(JpaContext.INSTANCE.entityManager()); }
  public DonacionRepository(EntityManager entityManager) { super(entityManager); }
  public void guardar(Donacion donacion) { enTransaccion(() -> { if (donacion.getId() == null) entityManager().persist(donacion); else entityManager().merge(donacion); }); }
  public List<Donacion> obtenerTodas() { return entityManager().createQuery("from Donacion", Donacion.class).getResultList(); }
  public Optional<Donacion> buscarPorId(Long id) { return Optional.ofNullable(entityManager().find(Donacion.class, id)); }
  public void borrarPorId(Long id) { enTransaccion(() -> { Donacion d = entityManager().find(Donacion.class, id); if (d != null) entityManager().remove(d); }); }
}
