package donaciones.domain.eventos;

import donaciones.domain.Donacion;

public class EntregaRealizadaEvent implements CambioDeEstadoEnDonacion {
    private final Donacion donacion;
    private final String fechaHora;
    private final String camionResponsable;

    public EntregaRealizadaEvent(Donacion donacion, String fechaHora, String camionResponsable) {
        this.donacion = donacion;
        this.fechaHora = fechaHora;
        this.camionResponsable = camionResponsable;
    }

    public Donacion getDonacion() {
        return donacion;
    }

    public String getFechaHora() {
        return fechaHora;
    }

    public String getCamionResponsable() {
        return camionResponsable;
    }
}
