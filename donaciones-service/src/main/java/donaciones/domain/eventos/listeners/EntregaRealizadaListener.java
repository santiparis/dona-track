package donaciones.domain.eventos.listeners;

import donaciones.domain.eventos.CambioDeEstadoEnDonacion;
import donaciones.domain.eventos.EntregaRealizadaEvent;

public class EntregaRealizadaListener implements Listener {
    @Override
    public void onEvento(CambioDeEstadoEnDonacion evento) {
        EntregaRealizadaEvent e = (EntregaRealizadaEvent) evento;
        String comprobante = String.format("Comprobante de Entrega - Fecha/Hora: %s | Camión: %s",
                e.getFechaHora(), e.getCamionResponsable());

        e.getDonacion().getDonante().notificar("Su donación fue entregada con éxito. " + comprobante);
        e.getDonacion().getEntidadBeneficiaria().notificar("Donación recibida satisfactoriamente. " + comprobante);
    }
}
