import java.util.ArrayList;
import java.util.List;

public class Asignacion {
    private final Necesidad necesidad;
    private EstadoAsignacion estado;
    private final List<RegistroCambioEstado<EstadoAsignacion>> historialEstados = new ArrayList<>();
    private String justificacionEntregaFallida;

    public Asignacion(Necesidad necesidad, EstadoAsignacion estado) {
        this.necesidad = necesidad;
        this.estado = estado;
        this.historialEstados.add(new RegistroCambioEstado<>(null, estado, new java.util.Date(), null));
    }

    public void agregarBien(Bien nuevoBien) {
        this.bienes.add(nuevoBien);
    }

    public void reducirBienDeSubcategoria(Bien bien, Integer cantidad) {
        Bien bienAReducir = this.bienes.stream()
                .filter(b -> b.getSubcategoria() == bien.getSubcategoria())
                .findFirst()
                .orElse(null);
        if (bienAReducir != null) {
            int nuevaCantidad = bienAReducir.getCantidad() - cantidad;
            if (nuevaCantidad <= 0) {
                this.bienes.remove(bienAReducir);
            } else {
                this.bienes.remove(bienAReducir);
                this.bienes.add(bienAReducir.conCantidad(nuevaCantidad));
            }
        }
    }

    public List<Bien> getBienes() {
        return bienes;
    }

    public Necesidad getNecesidad() {
        return necesidad;
    }

    public EstadoAsignacion getEstado() {
        return this.estado;
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
