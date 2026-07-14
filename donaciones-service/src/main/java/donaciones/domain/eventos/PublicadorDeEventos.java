package donaciones.domain.eventos;

import donaciones.domain.eventos.listeners.Listener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PublicadorDeEventos {
    private final Map<Class, List<Listener>> suscriptores = new HashMap<>();

    public void suscribir(Class tipoEvento, Listener listener) {
        suscriptores
            .computeIfAbsent(tipoEvento, k -> new ArrayList<>())
            .add(listener);
    }

    public void publicar(CambioDeEstadoEnDonacion evento) {
        List<Listener> listeners = suscriptores.getOrDefault(evento.getClass(), List.of());
        for (Listener listener : listeners) {
            listener.onEvento(evento);
        }
    }

    public void limpiar() {
        suscriptores.clear();
    }
}
