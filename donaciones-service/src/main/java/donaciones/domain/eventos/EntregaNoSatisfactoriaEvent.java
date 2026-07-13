package donaciones.domain.eventos;

import donaciones.domain.EntidadBeneficiaria;
import donaciones.domain.PersonaAdministradora;
import donaciones.domain.donante.Persona;
import java.util.List;

public class EntregaNoSatisfactoriaEvent implements CambioDeEstadoEnDonacion {
    private final Persona donante;
    private final EntidadBeneficiaria entidad;
    private final List<PersonaAdministradora> administradores;

    public EntregaNoSatisfactoriaEvent(Persona donante, EntidadBeneficiaria entidad, List<PersonaAdministradora> administradores) {
        this.donante = donante;
        this.entidad = entidad;
        this.administradores = administradores;
    }

    @Override
    public void notificarAInvolucrados() {
        String mensaje = "Alerta: Entrega no satisfactoria.";

        donante.notificar(mensaje);
        entidad.notificar(mensaje);
        administradores.forEach(admin -> admin.notificar(mensaje));
    }
}
