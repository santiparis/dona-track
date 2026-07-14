package donaciones.domain.eventos;

import donaciones.domain.Donacion;

public class InicioRutaEvent implements CambioDeEstadoEnDonacion {
    private final Donacion donacion;
    private final String urlMapaSeguimiento;

    public InicioRutaEvent(Donacion donacion, String urlMapaSeguimiento) {
        this.donacion = donacion;
        this.urlMapaSeguimiento = urlMapaSeguimiento;
    }

    public Donacion getDonacion() {
        return donacion;
    }

    public String getUrlMapaSeguimiento() {
        return urlMapaSeguimiento;
    }
}
