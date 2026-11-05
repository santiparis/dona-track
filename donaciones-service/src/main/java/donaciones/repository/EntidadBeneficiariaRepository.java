package donaciones.repository;

import donaciones.domain.EntidadBeneficiaria;
import donaciones.db.TransaccionPorRequest;
import io.github.flbulgarelli.jpa.extras.simple.WithSimplePersistenceUnit;

import javax.persistence.LockModeType;
import java.util.Map;
import java.util.List;
import java.util.Optional;

public class EntidadBeneficiariaRepository implements WithSimplePersistenceUnit {
  public List<EntidadBeneficiaria> obtenerTodas() {
    TransaccionPorRequest.iniciarSiEsNecesario();
    return createQuery("from EntidadBeneficiaria", EntidadBeneficiaria.class).getResultList();
  }

    public Optional<EntidadBeneficiaria> buscarPorUsuario(Long usuarioId) {
        TransaccionPorRequest.iniciarSiEsNecesario();
        return createQuery("from EntidadBeneficiaria p where p.usuario.id = :usuarioId", EntidadBeneficiaria.class)
                .setParameter("usuarioId", usuarioId)
                .getResultStream()
                .findFirst();
    }


  public Optional<EntidadBeneficiaria> buscarPorId(Long id) {
    TransaccionPorRequest.iniciarSiEsNecesario();
    return Optional.ofNullable(find(EntidadBeneficiaria.class, id));
  }

  public Optional<EntidadBeneficiaria> buscarPorIdParaActualizar(Long id) {
    TransaccionPorRequest.iniciarSiEsNecesario();
    entityManager().flush();
    return Optional.ofNullable(entityManager().find(
        EntidadBeneficiaria.class,
        id,
        LockModeType.PESSIMISTIC_WRITE,
        Map.of("javax.persistence.lock.timeout", 2000)
    ));
  }

  public Optional<EntidadBeneficiaria> obtenerPorId(Long id) { return buscarPorId(id); }

  public void guardar(EntidadBeneficiaria entidad) {
    TransaccionPorRequest.iniciarSiEsNecesario();
    if (entidad.getId() == null) persist(entidad);
  }

  public void eliminarPorId(Long id) {
    TransaccionPorRequest.iniciarSiEsNecesario();
    buscarPorIdParaActualizar(id).ifPresent(this::remove);
  }

  public void eliminar(EntidadBeneficiaria entidad) {
    TransaccionPorRequest.iniciarSiEsNecesario();
    remove(entidad);
  }
}
