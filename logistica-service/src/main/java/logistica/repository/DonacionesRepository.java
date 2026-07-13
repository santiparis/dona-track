package logistica.repository;

import logistica.domain.Donacion;

import java.util.ArrayList;
import java.util.List;

public class DonacionesRepository {

  private final List<Donacion> donaciones = new ArrayList<>();

  public void agregar(Donacion donacion) {
    donaciones.add(donacion);
  }

  public void agregarTodos(List<Donacion> nuevas) {
    donaciones.addAll(nuevas);
  }

  public List<Donacion> obtenerTodas() {
    return new ArrayList<>(donaciones);
  }

  public void remover(List<Donacion> aRemover) {
    donaciones.removeAll(aRemover);
  }
}
