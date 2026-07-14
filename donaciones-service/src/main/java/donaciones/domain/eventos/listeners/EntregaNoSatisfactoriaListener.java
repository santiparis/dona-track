package donaciones.domain.eventos.listeners;

import donaciones.domain.eventos.CambioDeEstadoEnDonacion;
import donaciones.domain.eventos.EntregaNoSatisfactoriaEvent;
import donaciones.repository.PersonasAdministradorasRepository;

public class EntregaNoSatisfactoriaListener implements Listener {
    private final PersonasAdministradorasRepository repoAdmins;

    public EntregaNoSatisfactoriaListener(PersonasAdministradorasRepository repoAdmins) {
        this.repoAdmins = repoAdmins;
    }

    @Override
    public void onEvento(CambioDeEstadoEnDonacion evento) {
        EntregaNoSatisfactoriaEvent e = (EntregaNoSatisfactoriaEvent) evento;
        String prefijo = "[Donación #" + e.getDonacion().getID() + "] ";
        String mensaje = prefijo + "Alerta: Entrega no satisfactoria.";
        e.getDonacion().getDonante().notificar(mensaje);
        e.getDonacion().getEntidadBeneficiaria().notificar(mensaje);
        repoAdmins.obtenerTodos().forEach(admin -> admin.notificar(mensaje));
    }
}
