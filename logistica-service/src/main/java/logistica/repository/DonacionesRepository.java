package logistica.repository;

import logistica.domain.DonacionEncolada;

import java.util.ArrayList;
import java.util.List;

public class DonacionesRepository {

  private final List<DonacionEncolada> donaciones = new ArrayList<>();

  public void agregar(DonacionEncolada donacionEncolada) {
    donaciones.add(donacionEncolada);
  }

  public void agregarTodos(List<DonacionEncolada> nuevas) {
    donaciones.addAll(nuevas);
  }

  public List<DonacionEncolada> obtenerTodas() {
    return new ArrayList<>(donaciones);
  }

  public void remover(List<DonacionEncolada> aRemover) {
    donaciones.removeAll(aRemover);
  }
}
