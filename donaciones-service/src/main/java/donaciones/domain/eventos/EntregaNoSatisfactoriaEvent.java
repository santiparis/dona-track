package donaciones.domain.eventos;

import donaciones.domain.EntidadBeneficiaria;
import donaciones.domain.PersonaAdministradora;
import donaciones.domain.donante.Persona;
import java.util.List;

public class EntregaNoSatisfactoriaEvent {
    private final Persona donante;
    private final EntidadBeneficiaria entidad;
    private final List<PersonaAdministradora> administradores;
    private final String motivoFallo;

    public EntregaNoSatisfactoriaEvent(Persona donante, EntidadBeneficiaria entidad, List<PersonaAdministradora> administradores, String motivoFallo) {
        this.donante = donante;
        this.entidad = entidad;
        this.administradores = administradores;
        this.motivoFallo = motivoFallo;
    }

    public Persona getDonante() {
        return donante;
    }

    public EntidadBeneficiaria getEntidad() {
        return entidad;
    }

    public List<PersonaAdministradora> getAdministradores() {
        return administradores;
    }

    public String getMotivoFallo() {
        return motivoFallo;
    }
}
