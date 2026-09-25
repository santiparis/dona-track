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
  public List<EntidadBeneficiaria> obtenerTodas() {
    List<EntidadBeneficiaria> entidades = entityManager().createQuery("from EntidadBeneficiaria", EntidadBeneficiaria.class).getResultList();
    entidades.forEach(this::reconstruirContactos);
    return entidades;
  }
  public Optional<EntidadBeneficiaria> buscarPorId(Long id) {
    EntidadBeneficiaria entidad = entityManager().find(EntidadBeneficiaria.class, id);
    reconstruirContactos(entidad);
    return Optional.ofNullable(entidad);
  }
  public Optional<EntidadBeneficiaria> obtenerPorId(Long id) { return buscarPorId(id); }
  public void guardar(EntidadBeneficiaria entidad) { enTransaccion(() -> { if (entidad.getId() == null) entityManager().persist(entidad); else entityManager().merge(entidad); }); }
  public void eliminarPorId(Long id) { enTransaccion(() -> { EntidadBeneficiaria e = entityManager().find(EntidadBeneficiaria.class, id); if (e != null) entityManager().remove(e); }); }

  private void reconstruirContactos(EntidadBeneficiaria entidad) {
    if (entidad != null && entidad.getId() != null) {
      List<donaciones.domain.notificacion.Contacto> contactos = new ContactoRepository(entityManager())
          .buscarPorNotificable(entidad.getId(), "ENTIDAD");
      if (!contactos.isEmpty()) entidad.reconstruirContactos(contactos);
    }
  }
}
