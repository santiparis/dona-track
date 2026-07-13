package donaciones.domain.eventos;

import donaciones.domain.EntidadBeneficiaria;
import donaciones.domain.donante.Persona;

public class DonacionAsignadaEvent implements CambioDeEstadoEnDonacion {
    private final Persona donante;
    private final EntidadBeneficiaria entidadBeneficiaria;

    public DonacionAsignadaEvent(Persona donante, EntidadBeneficiaria entidadBeneficiaria) {
        this.donante = donante;
        this.entidadBeneficiaria = entidadBeneficiaria;
    }

    @Override
    public void notificarAInvolucrados() {
        donante.notificar("Su donación ha sido asignada a la entidad: " + entidadBeneficiaria.getRazonSocial());
        entidadBeneficiaria.notificar("Se le ha asignado satisfactoriamente una nueva donación.");
    }
}
