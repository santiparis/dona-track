package donaciones.domain.eventos.listeners;

import donaciones.domain.eventos.CambioDeEstadoEnDonacion;
import donaciones.domain.eventos.EntregaRealizadaEvent;

public class EntregaRealizadaListener implements Listener {
    @Override
    public void onEvento(CambioDeEstadoEnDonacion evento) {
        EntregaRealizadaEvent e = (EntregaRealizadaEvent) evento;
        String prefijo = "[Donación #" + e.getDonacion().getID() + "] ";
        String comprobante = String.format("Comprobante de Entrega - Fecha/Hora: %s | Camión: %s",
                e.getFechaHora(), e.getCamionResponsable());

        e.getDonacion().getDonante().notificar(prefijo + "Su donación fue entregada con éxito. " + comprobante);
        e.getDonacion().getEntidadBeneficiaria().notificar(prefijo + "Donación recibida satisfactoriamente. " + comprobante);
    }
}
