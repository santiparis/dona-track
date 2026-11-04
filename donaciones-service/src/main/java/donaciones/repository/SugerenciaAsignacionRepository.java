package donaciones.repository;

import donaciones.domain.algoritmos.SugerenciaAsignacion;
import donaciones.db.TransaccionPorRequest;
import io.github.flbulgarelli.jpa.extras.simple.WithSimplePersistenceUnit;

import javax.persistence.LockModeType;
import java.util.Map;
import java.util.List;
import java.util.Optional;

public class SugerenciaAsignacionRepository implements WithSimplePersistenceUnit {
  public void guardar(SugerenciaAsignacion sugerencia) {
    TransaccionPorRequest.iniciarSiEsNecesario();
    if (find(SugerenciaAsignacion.class, sugerencia.getId()) == null) persist(sugerencia);
  }

  public List<SugerenciaAsignacion> obtenerTodas() {
    TransaccionPorRequest.iniciarSiEsNecesario();
    return createQuery("from SugerenciaAsignacion", SugerenciaAsignacion.class).getResultList();
  }

  public void limpiar() {
    TransaccionPorRequest.iniciarSiEsNecesario();
    createQuery("delete from SugerenciaAsignacion").executeUpdate();
  }

  public void eliminar(SugerenciaAsignacion sugerencia) {
    TransaccionPorRequest.iniciarSiEsNecesario();
    buscarPorID(sugerencia.getId()).ifPresent(this::remove);
  }

  public Optional<SugerenciaAsignacion> buscarPorID(Long id) {
    TransaccionPorRequest.iniciarSiEsNecesario();
    return Optional.ofNullable(find(SugerenciaAsignacion.class, id));
  }

  public Optional<SugerenciaAsignacion> buscarPorIDParaActualizar(Long id) {
    TransaccionPorRequest.iniciarSiEsNecesario();
    entityManager().flush();
    return Optional.ofNullable(entityManager().find(
        SugerenciaAsignacion.class,
        id,
        LockModeType.PESSIMISTIC_WRITE,
        Map.of("javax.persistence.lock.timeout", 2000)
    ));
  }
}
