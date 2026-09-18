package logistica.repository;

import io.github.flbulgarelli.jpa.extras.simple.WithSimplePersistenceUnit;
import logistica.domain.DonacionEncolada;

import java.util.List;

public class DonacionesRepository implements WithSimplePersistenceUnit {

  public void agregar(DonacionEncolada donacionEncolada) {
    persist(donacionEncolada);
  }

  public void agregarTodos(List<DonacionEncolada> nuevas) {
    nuevas.forEach(this::agregar);
  }

  public List<DonacionEncolada> obtenerTodas() {
    return createQuery("from DonacionEncolada", DonacionEncolada.class).getResultList();
  }

  public void remover(List<DonacionEncolada> aRemover) {
    aRemover.forEach(this::remove);
  }
}