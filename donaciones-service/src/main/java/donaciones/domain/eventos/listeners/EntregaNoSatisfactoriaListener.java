package donaciones.domain.eventos.listeners;

import donaciones.domain.eventos.CambioDeEstadoEnDonacion;
import donaciones.domain.eventos.EntregaNoSatisfactoriaEvent;
import donaciones.repository.RepositorioPersonasAdministradoras;

public class EntregaNoSatisfactoriaListener implements Listener {
    private final RepositorioPersonasAdministradoras repoAdmins;

    public EntregaNoSatisfactoriaListener(RepositorioPersonasAdministradoras repoAdmins) {
        this.repoAdmins = repoAdmins;
    }

    @Override
    public void onEvento(CambioDeEstadoEnDonacion evento) {
        EntregaNoSatisfactoriaEvent e = (EntregaNoSatisfactoriaEvent) evento;
        String mensaje = "Alerta: Entrega no satisfactoria.";
        e.getDonante().notificar(mensaje);
        e.getEntidad().notificar(mensaje);
        repoAdmins.obtenerTodos().forEach(admin -> admin.notificar(mensaje));
    }
}
