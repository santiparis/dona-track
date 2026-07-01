package donaciones.domain.eventos;

import donaciones.domain.eventos.listeners.Listener;
import java.util.ArrayList;
import java.util.List;

public class Publicador<T> {
    private final List<Listener<T>> suscriptores = new ArrayList<>();

    public void suscribir(Listener<T> listener) {
        suscriptores.add(listener);
    }

    public void desuscribir(Listener<T> listener) {
        suscriptores.remove(listener);
    }

    public void publicar(T evento) {
        suscriptores.forEach(s -> s.onEvento(evento));
    }
}
