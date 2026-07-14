package donaciones.domain.eventos;

import donaciones.domain.Donacion;

public class InicioRutaEvent implements CambioDeEstadoEnDonacion {
    private final Donacion donacion;
    private final String urlMapaSeguimiento;

    public InicioRutaEvent(Donacion donacion, String urlMapaSeguimiento) {
        this.donacion = donacion;
        this.urlMapaSeguimiento = urlMapaSeguimiento;
    }

    @Override
    public void notificarAInvolucrados() {
        String mensaje = "Su entrega está en camino. Siga el recorrido en tiempo real aquí: " + urlMapaSeguimiento;

        donacion.getDonante().notificar(mensaje);
        donacion.getEntidadBeneficiaria().notificar(mensaje);
    }
}
