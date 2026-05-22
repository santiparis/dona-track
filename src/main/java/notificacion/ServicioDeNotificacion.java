package notificacion;

import donante.Contacto;
import donante.Persona;
import donante.TipoContacto;
import java.util.Optional;

public class ServicioDeNotificacion {

    public void enviarEmailDeBienvenida(Persona persona) {
        Optional<Contacto> contactoEmail = persona.getContactos().stream()
                .filter(c -> c.getTipo() == TipoContacto.EMAIL)
                .findFirst();

        contactoEmail.ifPresent(contacto -> {
            String mensaje = "¡Hola " + persona.getNombre() + "! Te damos la bienvenida a dona-track. Gracias por unirte.";
            this.enviar(contacto, mensaje);
        });
    }

    public void enviar(Contacto contacto, String mensaje) {
        EstrategiaDeNotificacion estrategia = crearEstrategia(contacto.getTipo());
        Notificacion notificacion = new Notificacion(contacto, mensaje);
        boolean exito = estrategia.enviar(notificacion);

        if (exito) {
            notificacion.marcarComoCompletada();
        } else {
            notificacion.marcarComoFallida();
        }
    }

    private EstrategiaDeNotificacion crearEstrategia(TipoContacto tipo) {
        return switch (tipo) {
            case EMAIL -> new NotificacionPorEmail();
            case SMS -> new NotificacionPorSMS();
            case WHATSAPP -> new NotificacionPorWhatsApp();
        };
    }
}