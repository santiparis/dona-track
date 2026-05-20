package notificacion;

public class Notificacion {
    private final String destinatario;
    private final String mensaje;
    private EstadoNotificacion estado;

    public Notificacion(String destinatario, String mensaje) {
        this.destinatario = destinatario;
        this.mensaje = mensaje;
        this.estado = EstadoNotificacion.PENDIENTE;
    }

    public String getDestinatario() {
        return destinatario;
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