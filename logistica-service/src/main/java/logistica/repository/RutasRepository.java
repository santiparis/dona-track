package logistica.repository;

import logistica.domain.Entrega;
import logistica.domain.Ruta;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class RutasRepository {
  private final List<Ruta> rutas = new ArrayList<>();
  private static long rutaIdSequence = 1L;
  private static long entregaIdSequence = 1L;

  public void agregar(Ruta ruta) {
    if (ruta.getId() == null) {
      ruta.setId(rutaIdSequence++);
    }
    for (Entrega entrega : ruta.getEntregas()) {
      if (entrega.getId() == null) {
        entrega.setId(entregaIdSequence++);
      }
    }
    rutas.add(ruta);
  }

  public void agregarTodos(List<Ruta> nuevas) {
    for (Ruta ruta : nuevas) {
      if (ruta.getId() == null) {
        ruta.setId(rutaIdSequence++);
      }
      for (Entrega entrega : ruta.getEntregas()) {
        if (entrega.getId() == null) {
          entrega.setId(entregaIdSequence++);
        }
      }
    }
    rutas.addAll(nuevas);
  }

  public Optional<Ruta> buscarPorId(Long id) {
    return rutas.stream()
        .filter(r -> r.getId() != null && r.getId().equals(id))
        .findFirst();
  }

  public Optional<Entrega> buscarEntregaPorId(Long entregaId) {
    return rutas.stream()
        .flatMap(r -> r.getEntregas().stream())
        .filter(e -> e.getId() != null && e.getId().equals(entregaId))
        .findFirst();
  }

  public List<Ruta> obtenerTodas() {
    return new ArrayList<>(rutas);
  }

  public Optional<Ruta> buscarRutaPorEntregaId(Long entregaId) {
    return rutas.stream()
        .filter(r -> r.getEntregas().stream().anyMatch(e -> e.getId() != null && e.getId().equals(entregaId)))
        .findFirst();
  }
}