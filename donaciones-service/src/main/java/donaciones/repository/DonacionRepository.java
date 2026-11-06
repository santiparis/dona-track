package donaciones.repository;

import donaciones.domain.Donacion;
import donaciones.db.TransaccionPorRequest;
import io.github.flbulgarelli.jpa.extras.simple.WithSimplePersistenceUnit;

import javax.persistence.LockModeType;
import java.util.Map;
import java.util.List;
import java.util.Optional;

public class DonacionRepository implements WithSimplePersistenceUnit {
  public void guardar(Donacion donacion) {
    TransaccionPorRequest.iniciarSiEsNecesario();
    if (donacion.getId() == null) persist(donacion);
  }

  public List<Donacion> obtenerTodas() {
    TransaccionPorRequest.iniciarSiEsNecesario();
    return createQuery("from Donacion", Donacion.class).getResultList();
  }

  public List<Donacion> buscarPorUsuarioDonante(Long usuarioId) {
    TransaccionPorRequest.iniciarSiEsNecesario();
    return createQuery("from Donacion d where d.donante.usuario.id = :usuarioId order by d.fecha desc, d.id desc", Donacion.class)
        .setParameter("usuarioId", usuarioId).getResultList();
  }

  public List<Donacion> buscarPorUsuarioEntidad(Long usuarioId) {
    TransaccionPorRequest.iniciarSiEsNecesario();
    return createQuery("from Donacion d where d.entidadBeneficiaria.usuario.id = :usuarioId order by d.fecha desc, d.id desc", Donacion.class)
        .setParameter("usuarioId", usuarioId).getResultList();
  }

  public Optional<Donacion> buscarPorId(Long id) {
    TransaccionPorRequest.iniciarSiEsNecesario();
    return Optional.ofNullable(find(Donacion.class, id));
  }

  public Optional<Donacion> buscarPorIdParaActualizar(Long id) {
    TransaccionPorRequest.iniciarSiEsNecesario();
    entityManager().flush();
    return Optional.ofNullable(entityManager().find(
        Donacion.class,
        id,
        LockModeType.PESSIMISTIC_WRITE,
        Map.of("javax.persistence.lock.timeout", 2000)
    ));
  }

  public void borrarPorId(Long id) {
    TransaccionPorRequest.iniciarSiEsNecesario();
    buscarPorIdParaActualizar(id).ifPresent(this::remove);
  }

  public void eliminar(Donacion donacion) {
    TransaccionPorRequest.iniciarSiEsNecesario();
    remove(donacion);
  }
}
