package donaciones.domain.eventos.listeners;

import donaciones.domain.eventos.InicioRutaEvent;

public class InicioRutaListener implements Listener<InicioRutaEvent> {

    @Override
    public void onEvento(InicioRutaEvent evento) {
        String mensaje = "Su entrega está en camino. Siga el recorrido en tiempo real aquí: " + evento.getUrlMapaSeguimiento();
        
        if (evento.getDonantes() != null) {
            evento.getDonantes().forEach(donante -> donante.notificar(mensaje));
        }
        if (evento.getEntidades() != null) {
            evento.getEntidades().forEach(entidad -> entidad.notificar(mensaje));
        }
    }
}
