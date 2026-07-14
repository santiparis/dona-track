package donaciones.domain.eventos;

import donaciones.domain.EntidadBeneficiaria;
import donaciones.domain.donante.Persona;

public class EntregaNoSatisfactoriaEvent implements CambioDeEstadoEnDonacion {
    private final Persona donante;
    private final EntidadBeneficiaria entidad;

    public EntregaNoSatisfactoriaEvent(Persona donante, EntidadBeneficiaria entidad) {
        this.donante = donante;
        this.entidad = entidad;
    }

    public Persona getDonante() {
        return donante;
    }

    public EntidadBeneficiaria getEntidad() {
        return entidad;
    }
}
