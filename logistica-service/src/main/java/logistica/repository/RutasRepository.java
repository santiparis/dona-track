package logistica.repository;

import io.github.flbulgarelli.jpa.extras.simple.WithSimplePersistenceUnit;
import logistica.domain.Entrega;
import logistica.domain.Ruta;

import java.util.List;
import java.util.Optional;

public class RutasRepository implements WithSimplePersistenceUnit {

  // la ruta arrastra sus entregas, y cada entrega sus donaciones: van en cascada
  public void agregar(Ruta ruta) {
    persist(ruta);
  }

  public Optional<Ruta> buscarPorId(Long id) {
    return Optional.ofNullable(find(Ruta.class, id));
  }

  public Optional<Entrega> buscarEntregaPorId(Long entregaId) {
    return Optional.ofNullable(find(Entrega.class, entregaId));
  }

  public List<Ruta> obtenerTodas() {
    return createQuery("from Ruta", Ruta.class).getResultList();
  }

  // la entrega no conoce a su ruta, asi que la pregunta se hace desde el lado que si la conoce
  public Optional<Ruta> buscarRutaPorEntregaId(Long entregaId) {
    return createQuery("select r from Ruta r join r.entregas e where e.id = :entregaId", Ruta.class)
        .setParameter("entregaId", entregaId)
        .getResultStream()
        .findFirst();
  }
}