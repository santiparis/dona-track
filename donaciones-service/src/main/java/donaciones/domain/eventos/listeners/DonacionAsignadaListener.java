package donaciones.domain.eventos.listeners;

import donaciones.domain.eventos.DonacionAsignadaEvent;

public class DonacionAsignadaListener implements Listener<DonacionAsignadaEvent> {

    @Override
    public void onEvento(DonacionAsignadaEvent evento) {
        if (evento.getDonante() != null) {
            evento.getDonante().notificar(
                "Su donación ha sido asignada a la entidad: " + evento.getEntidadBeneficiaria().getRazonSocial()
            );
        }
        if (evento.getEntidadBeneficiaria() != null) {
            evento.getEntidadBeneficiaria().notificar(
                "Se le ha asignado satisfactoriamente una nueva donación."
            );
        }
    }
}
