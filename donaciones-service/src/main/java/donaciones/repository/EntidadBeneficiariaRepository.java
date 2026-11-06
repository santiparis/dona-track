package donaciones.repository;

import donaciones.domain.EntidadBeneficiaria;
import donaciones.db.TransaccionPorRequest;
import io.github.flbulgarelli.jpa.extras.simple.WithSimplePersistenceUnit;

import java.util.List;
import java.util.Optional;

public class EntidadBeneficiariaRepository implements WithSimplePersistenceUnit {
  public List<EntidadBeneficiaria> obtenerTodas() {
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
    return Optional.ofNullable(find(EntidadBeneficiaria.class, id));
  }

  public Optional<EntidadBeneficiaria> buscarPorIdParaActualizar(Long id) {
    TransaccionPorRequest.iniciarSiEsNecesario();
    return Optional.ofNullable(find(EntidadBeneficiaria.class, id));
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
