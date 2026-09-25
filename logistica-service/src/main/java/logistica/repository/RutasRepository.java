package logistica.repository;

import io.github.flbulgarelli.jpa.extras.simple.WithSimplePersistenceUnit;
import logistica.domain.Entrega;
import logistica.domain.Ruta;

import java.util.List;
import java.util.Optional;

public class RutasRepository implements WithSimplePersistenceUnit {

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

  public Optional<Ruta> buscarRutaPorEntregaId(Long entregaId) {
    return createQuery("select r from Ruta r join r.entregas e where e.id = :entregaId", Ruta.class)
        .setParameter("entregaId", entregaId)
        .getResultStream()
        .findFirst();
  }
}