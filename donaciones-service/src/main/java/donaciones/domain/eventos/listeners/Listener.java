package donaciones.domain.eventos.listeners;

public interface Listener<T> {
    void onEvento(T evento);
}
