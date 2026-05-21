import java.util.ArrayList;
import java.util.List;

public class Asignacion {
    private final Necesidad necesidad;
    private final EstadoDonacion estado;

    public Asignacion(Necesidad necesidad, EstadoDonacion estado) {
        this.necesidad = necesidad;
        this.estado = estado;
        this.historialEstados.add(new RegistroCambioEstado<>(null, estado, new java.util.Date(), null));
    }


    public List<Bien> getBienes() {
        return bienes;
    }

    public Necesidad getNecesidad() {
        return necesidad;
    }

    public EstadoDonacion getEstado() {
        return estado;
    }

    public void setEstado(EstadoAsignacion estado) {
        this.setEstado(estado, null);
    }

    public void setEstado(EstadoAsignacion estado, String justificacion) {
        if (estado == EstadoAsignacion.ENTREGA_FALLIDA && (justificacion == null || justificacion.isBlank())) {
            throw new IllegalArgumentException("La entrega fallida requiere una justificación");
        }
        EstadoAsignacion estadoAnterior = this.estado;
        this.estado = estado;
        if (estado == EstadoAsignacion.ENTREGA_FALLIDA) {
            this.justificacionEntregaFallida = justificacion;
        }
        this.historialEstados.add(new RegistroCambioEstado<>(estadoAnterior, estado, new java.util.Date(), justificacion));
    }

    public String getJustificacionEntregaFallida() {
        return justificacionEntregaFallida;
    }

    public List<RegistroCambioEstado<EstadoAsignacion>> getHistorialEstados() {
        return historialEstados;
    }
}
