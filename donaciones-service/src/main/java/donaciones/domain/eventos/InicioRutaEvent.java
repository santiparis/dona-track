package donaciones.domain.eventos;

import donaciones.domain.EntidadBeneficiaria;
import donaciones.domain.donante.Persona;
import java.util.List;

public class InicioRutaEvent implements CambioDeEstadoEnDonacion {
    private final List<Persona> donantes;
    private final List<EntidadBeneficiaria> entidades;
    private final String urlMapaSeguimiento;

    public InicioRutaEvent(List<Persona> donantes, List<EntidadBeneficiaria> entidades, String urlMapaSeguimiento) {
        this.donantes = donantes;
        this.entidades = entidades;
        this.urlMapaSeguimiento = urlMapaSeguimiento;
    }

    @Override
    public void notificarAInvolucrados() {
        String mensaje = "Su entrega está en camino. Siga el recorrido en tiempo real aquí: " + urlMapaSeguimiento;

        donantes.forEach(donante -> donante.notificar(mensaje));
        entidades.forEach(entidad -> entidad.notificar(mensaje));
    }
}
