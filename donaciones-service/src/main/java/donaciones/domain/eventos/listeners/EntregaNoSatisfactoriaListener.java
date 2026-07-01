package donaciones.domain.eventos.listeners;

import donaciones.domain.eventos.EntregaNoSatisfactoriaEvent;

public class EntregaNoSatisfactoriaListener implements Listener<EntregaNoSatisfactoriaEvent> {

    @Override
    public void onEvento(EntregaNoSatisfactoriaEvent evento) {
        String mensaje = "Alerta: Entrega no satisfactoria. Motivo: " + evento.getMotivoFallo();

        if (evento.getDonante() != null) {
            evento.getDonante().notificar(mensaje);
        }
        if (evento.getEntidad() != null) {
            evento.getEntidad().notificar(mensaje);
        }
        if (evento.getAdministradores() != null) {
            evento.getAdministradores().forEach(admin -> admin.notificar(mensaje));
        }
    }
}
