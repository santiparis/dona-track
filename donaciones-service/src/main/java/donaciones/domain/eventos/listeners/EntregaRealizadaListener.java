package donaciones.domain.eventos.listeners;

import donaciones.domain.eventos.EntregaRealizadaEvent;

public class EntregaRealizadaListener implements Listener<EntregaRealizadaEvent> {

    @Override
    public void onEvento(EntregaRealizadaEvent evento) {
        String comprobante = String.format("Comprobante de Entrega - Fecha/Hora: %s | Camión: %s",
                evento.getFechaHora(), evento.getCamionResponsable());

        if (evento.getDonante() != null) {
            evento.getDonante().notificar(
                "Su donación fue entregada con éxito. " + comprobante
            );
        }
        if (evento.getEntidad() != null) {
            evento.getEntidad().notificar(
                "Donación recibida satisfactoriamente. " + comprobante
            );
        }
    }
}
