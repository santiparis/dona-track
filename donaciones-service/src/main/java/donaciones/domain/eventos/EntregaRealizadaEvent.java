package donaciones.domain.eventos;

import donaciones.domain.EntidadBeneficiaria;
import donaciones.domain.donante.Persona;

public class EntregaRealizadaEvent {
    private final Persona donante;
    private final EntidadBeneficiaria entidad;
    private final String fechaHora;
    private final String camionResponsable;

    public EntregaRealizadaEvent(Persona donante, EntidadBeneficiaria entidad, String fechaHora, String camionResponsable) {
        this.donante = donante;
        this.entidad = entidad;
        this.fechaHora = fechaHora;
        this.camionResponsable = camionResponsable;
    }

    public Persona getDonante() {
        return donante;
    }

    public EntidadBeneficiaria getEntidad() {
        return entidad;
    }

    public String getFechaHora() {
        return fechaHora;
    }

    public String getCamionResponsable() {
        return camionResponsable;
    }
}
