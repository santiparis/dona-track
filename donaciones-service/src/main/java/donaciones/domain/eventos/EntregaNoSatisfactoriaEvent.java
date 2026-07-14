package donaciones.domain.eventos;

import donaciones.domain.Donacion;

public class EntregaNoSatisfactoriaEvent implements CambioDeEstadoEnDonacion {
    private final Donacion donacion;

    public EntregaNoSatisfactoriaEvent(Donacion donacion) {
        this.donacion = donacion;
    }

    public Donacion getDonacion() {
        return donacion;
    }
}
