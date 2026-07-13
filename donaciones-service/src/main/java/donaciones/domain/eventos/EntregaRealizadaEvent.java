package donaciones.domain.eventos;

import donaciones.domain.EntidadBeneficiaria;
import donaciones.domain.donante.Persona;

public class EntregaRealizadaEvent implements CambioDeEstadoEnDonacion {
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

    @Override
    public void notificarAInvolucrados() {
        String comprobante = String.format("Comprobante de Entrega - Fecha/Hora: %s | Camión: %s",
                fechaHora, camionResponsable);

        donante.notificar("Su donación fue entregada con éxito. " + comprobante);
        entidad.notificar("Donación recibida satisfactoriamente. " + comprobante);
    }
}
