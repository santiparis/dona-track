package notificacion;

import donante.Contacto;

public class Notificacion {
    private final String mensaje;
    private EstadoNotificacion estado;
    private final Contacto contacto;

    public Notificacion(Contacto contacto, String mensaje) {
        this.contacto = contacto;
        this.mensaje = mensaje;
        this.estado = EstadoNotificacion.PENDIENTE;
    }

    public String getMensaje() {
        return mensaje;
    }

    public EstadoNotificacion getEstado() {
        return estado;
    }

    public void marcarComoCompletada() {
        this.estado = EstadoNotificacion.COMPLETADA;
    }

    public void marcarComoFallida() {
        this.estado = EstadoNotificacion.FALLIDA;
    }

    public void enviar() {
        boolean exito = contacto.enviar(mensaje);
        if (exito) {
            marcarComoCompletada();
        } else {
            marcarComoFallida();
        }
    }
}