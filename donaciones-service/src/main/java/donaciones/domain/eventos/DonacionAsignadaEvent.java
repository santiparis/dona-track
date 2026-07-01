package donaciones.domain.eventos;

import donaciones.domain.EntidadBeneficiaria;
import donaciones.domain.donante.Persona;

public class DonacionAsignadaEvent {
    private final Persona donante;
    private final EntidadBeneficiaria entidadBeneficiaria;

    public DonacionAsignadaEvent(Persona donante, EntidadBeneficiaria entidadBeneficiaria) {
        this.donante = donante;
        this.entidadBeneficiaria = entidadBeneficiaria;
    }

    public Persona getDonante() {
        return donante;
    }

    public EntidadBeneficiaria getEntidadBeneficiaria() {
        return entidadBeneficiaria;
    }
}
