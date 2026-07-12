package logistica.repository;

import logistica.domain.Entrega;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class RepositorioEntregas {
    private final List<Entrega> entregas = new ArrayList<>();

    public void agregar(Entrega entrega) {
        entregas.add(entrega);
    }

    public void agregarTodos(List<Entrega> nuevas) {
        entregas.addAll(nuevas);
    }

    public Optional<Entrega> buscarPorId(String id) {
        return entregas.stream()
            .filter(e -> e.getId().equals(id))
            .findFirst();
    }

    public List<Entrega> obtenerTodas() {
        return new ArrayList<>(entregas); // devuelve una copia para proteger la lista interna
    }
}