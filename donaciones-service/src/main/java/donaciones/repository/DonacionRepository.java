package donaciones.repository;

import donaciones.domain.Donacion;
import donaciones.db.TransaccionPorRequest;
import donaciones.domain.EstadoDonacion;
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

  public List<Donacion> buscarPorDonanteYEstado(Long donanteId, EstadoDonacion estado) {
    TransaccionPorRequest.iniciarSiEsNecesario();
    return createQuery("from Donacion d where d.donante.id = :donanteId and d.estado = :estado", Donacion.class)
        .setParameter("donanteId", donanteId)
        .setParameter("estado", estado)
        .getResultList();
  }

  public List<Donacion> buscarPorEntidadYEstado(Long entidadId, EstadoDonacion estado) {
    TransaccionPorRequest.iniciarSiEsNecesario();
    return createQuery("from Donacion d where d.entidadBeneficiaria.id = :entidadId and d.estado = :estado", Donacion.class)
        .setParameter("entidadId", entidadId)
        .setParameter("estado", estado)
        .getResultList();
  }

  public List<Donacion> buscarPorEntidad(Long entidadId) {
    TransaccionPorRequest.iniciarSiEsNecesario();
    return createQuery("from Donacion d where d.entidadBeneficiaria.id = :entidadId", Donacion.class)
        .setParameter("entidadId", entidadId)
        .getResultList();
  }

  public List<Donacion> buscarPorEstado(EstadoDonacion estado) {
    TransaccionPorRequest.iniciarSiEsNecesario();
    return createQuery("from Donacion d where d.estado = :estado", Donacion.class)
        .setParameter("estado", estado)
        .getResultList();
  }
}
