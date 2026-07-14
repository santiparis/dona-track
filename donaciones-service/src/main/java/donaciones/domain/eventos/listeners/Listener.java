package donaciones.domain.eventos.listeners;

import donaciones.domain.eventos.CambioDeEstadoEnDonacion;

public interface Listener {
    void onEvento(CambioDeEstadoEnDonacion evento);
}
