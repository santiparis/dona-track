package notificacion;

import donante.Contacto;

/**
 * Clase interna que modela los datos de una notificación.
 * Su visibilidad es de paquete para que solo pueda ser gestionada
 * por el ServicioDeNotificacion.
 */
class Notificacion {
    private final Contacto contacto;
    private final String mensaje;
    private EstadoNotificacion estado;

    Notificacion(Contacto contacto, String mensaje) {
        this.contacto = contacto;
        this.mensaje = mensaje;
        this.estado = EstadoNotificacion.PENDIENTE;
    }

    public Contacto getContacto() {
        return contacto;
    }

    public String getMensaje() {
        return mensaje;
    }

    public EstadoNotificacion getEstado() {
        return estado;
    }

    void marcarComoCompletada() {
        this.estado = EstadoNotificacion.COMPLETADA;
    }

    void marcarComoFallida() {
        this.estado = EstadoNotificacion.FALLIDA;
    }
}