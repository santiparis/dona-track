package donaciones.domain.eventos;

import donaciones.domain.Donacion;

public class DonacionAsignadaEvent implements CambioDeEstadoEnDonacion {
    private final Donacion donacion;

    public DonacionAsignadaEvent(Donacion donacion) {
        this.donacion = donacion;
    }

    public Donacion getDonacion() {
        return donacion;
    }
}
