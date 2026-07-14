package logistica.repository;

import logistica.domain.EstadoRuta;
import logistica.domain.Entrega;
import logistica.domain.Ruta;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class RutasRepository {
  private final List<Ruta> rutas = new ArrayList<>();

  public void agregar(Ruta ruta) {
    rutas.add(ruta);
  }

  public void agregarTodos(List<Ruta> nuevas) {
    rutas.addAll(nuevas);
  }

  public Optional<Ruta> buscarPorId(String id) {
    return rutas.stream()
        .filter(r -> r.getId().equals(id))
        .findFirst();
  }

  public Optional<Entrega> buscarEntregaPorId(String entregaId) {
    return rutas.stream()
        .flatMap(r -> r.getEntregas().stream())
        .filter(e -> e.getId().equals(entregaId))
        .findFirst();
  }

  public List<Ruta> obtenerTodas() {
    return new ArrayList<>(rutas);
  }

  public Optional<Ruta> buscarRutaPorEntregaId(String entregaId) {
    return rutas.stream()
        .filter(r -> r.getEntregas().stream().anyMatch(e -> e.getId().equals(entregaId)))
        .findFirst();
  }

  public Optional<Ruta> buscarRutaEnCursoPorCamion(String patente) {
    return rutas.stream()
        .filter(r -> r.getCamion().getPatente().equals(patente))
        .filter(r -> r.getEstado() == EstadoRuta.EN_CURSO)
        .findFirst();
  }
}