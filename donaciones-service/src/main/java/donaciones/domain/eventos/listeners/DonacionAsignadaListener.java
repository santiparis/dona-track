package donaciones.domain.eventos.listeners;

import donaciones.domain.eventos.CambioDeEstadoEnDonacion;
import donaciones.domain.eventos.DonacionAsignadaEvent;

public class DonacionAsignadaListener implements Listener {
    @Override
    public void onEvento(CambioDeEstadoEnDonacion evento) {
        DonacionAsignadaEvent e = (DonacionAsignadaEvent) evento;
        e.getDonacion().getDonante().notificar(
            "Su donación ha sido asignada a la entidad: " + e.getDonacion().getEntidadBeneficiaria().getRazonSocial());
        e.getDonacion().getEntidadBeneficiaria().notificar(
            "Se le ha asignado satisfactoriamente una nueva donación.");
    }
}
