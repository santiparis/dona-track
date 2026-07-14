package donaciones.domain.eventos.listeners;

import donaciones.domain.eventos.CambioDeEstadoEnDonacion;
import donaciones.domain.eventos.InicioRutaEvent;

public class InicioRutaListener implements Listener {
    @Override
    public void onEvento(CambioDeEstadoEnDonacion evento) {
        InicioRutaEvent e = (InicioRutaEvent) evento;
        String prefijo = "[Donación #" + e.getDonacion().getID() + "] ";
        String mensaje = prefijo + "Su entrega está en camino. Siga el recorrido en tiempo real aquí: " + e.getUrlMapaSeguimiento();

        e.getDonacion().getDonante().notificar(mensaje);
        e.getDonacion().getEntidadBeneficiaria().notificar(mensaje);
    }
}
