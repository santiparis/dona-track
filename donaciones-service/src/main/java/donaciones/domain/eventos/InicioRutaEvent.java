package donaciones.domain.eventos;

import donaciones.domain.EntidadBeneficiaria;
import donaciones.domain.donante.Persona;
import java.util.List;

public class InicioRutaEvent {
    private final List<Persona> donantes;
    private final List<EntidadBeneficiaria> entidades;
    private final String urlMapaSeguimiento;

    public InicioRutaEvent(List<Persona> donantes, List<EntidadBeneficiaria> entidades, String urlMapaSeguimiento) {
        this.donantes = donantes;
        this.entidades = entidades;
        this.urlMapaSeguimiento = urlMapaSeguimiento;
    }

    public List<Persona> getDonantes() {
        return donantes;
    }

    public List<EntidadBeneficiaria> getEntidades() {
        return entidades;
    }

    public String getUrlMapaSeguimiento() {
        return urlMapaSeguimiento;
    }
}
