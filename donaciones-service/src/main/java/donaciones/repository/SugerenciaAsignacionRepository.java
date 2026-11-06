package donaciones.repository;

import donaciones.domain.algoritmos.SugerenciaAsignacion;
import donaciones.db.TransaccionPorRequest;
import io.github.flbulgarelli.jpa.extras.simple.WithSimplePersistenceUnit;

import java.util.List;
import java.util.Optional;

public class SugerenciaAsignacionRepository implements WithSimplePersistenceUnit {
  public void guardar(SugerenciaAsignacion sugerencia) {
    TransaccionPorRequest.iniciarSiEsNecesario();
    if (find(SugerenciaAsignacion.class, sugerencia.getId()) == null) persist(sugerencia);
  }

  public List<SugerenciaAsignacion> obtenerTodas() {
    return createQuery("from SugerenciaAsignacion", SugerenciaAsignacion.class).getResultList();
  }

  public void limpiar() {
    TransaccionPorRequest.iniciarSiEsNecesario();
    obtenerTodas().forEach(this::remove);
  }

  public void eliminar(SugerenciaAsignacion sugerencia) {
    TransaccionPorRequest.iniciarSiEsNecesario();
    buscarPorID(sugerencia.getId()).ifPresent(this::remove);
  }

  public Optional<SugerenciaAsignacion> buscarPorID(Long id) {
    return Optional.ofNullable(find(SugerenciaAsignacion.class, id));
  }

  public Optional<SugerenciaAsignacion> buscarPorIDParaActualizar(Long id) {
    TransaccionPorRequest.iniciarSiEsNecesario();
    return Optional.ofNullable(find(SugerenciaAsignacion.class, id));
  }
}
