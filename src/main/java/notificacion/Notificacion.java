package notificacion;

public class Notificacion {
    private final String mensaje;
    private EstadoNotificacion estado;

    public Notificacion(String mensaje) {
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
}