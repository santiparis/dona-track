package logistica.repository;

import io.github.flbulgarelli.jpa.extras.simple.WithSimplePersistenceUnit;
import logistica.domain.Camion;

import java.util.List;
import java.util.Optional;

public class CamionesRepository implements WithSimplePersistenceUnit {

  public void agregar(Camion camion) {
    persist(camion);
  }

  public void agregarTodos(List<Camion> nuevos) {
    nuevos.forEach(this::agregar);
  }

  public Optional<Camion> buscarPorId(Long id) {
    return Optional.ofNullable(find(Camion.class, id));
  }

  // find solo busca por id, asi que por patente hace falta una consulta
  public Optional<Camion> buscarPorPatente(String patente) {
    return createQuery("from Camion where patente = :patente", Camion.class)
        .setParameter("patente", patente)
        .getResultStream()
        .findFirst();
  }

  public List<Camion> obtenerTodos() {
    return createQuery("from Camion", Camion.class).getResultList();
  }

  // el filtro va en la consulta: traer la flota entera para descartar la mayoria seria al pedo
  public List<Camion> obtenerDisponibles() {
    return createQuery("from Camion where disponibilidad = true", Camion.class).getResultList();
  }

  public void eliminarPorId(Long id) {
    this.buscarPorId(id).ifPresent(this::remove);
  }
}