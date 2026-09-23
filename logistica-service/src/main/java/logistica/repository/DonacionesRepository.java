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

  // la tabla tambien guarda las donaciones que ya van en una entrega (cascade desde Ruta)
  // pendientes son solo las que todavia no entraron en ninguna
  public List<DonacionEncolada> obtenerTodas() {
    return createQuery(
        "from DonacionEncolada d where d not in "
            + "(select dd from Entrega e join e.listaDonaciones dd)", DonacionEncolada.class)
        .getResultList();
  }

  public void remover(List<DonacionEncolada> aRemover) {
    aRemover.forEach(this::remove);
  }
}