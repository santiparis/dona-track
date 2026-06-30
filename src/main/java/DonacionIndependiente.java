import java.util.ArrayList;
import java.util.List;
import java.time.LocalDate;
import java.time.ZoneId;

public class DonacionIndependiente {
    private final Bien bien;
    private EstadoDonacionIndependiente estado = EstadoDonacionIndependiente.EN_DEPOSITO;
    private int usado = 0;
    private final List<AsignacionItem<Necesidad, Integer>> asignaciones = new ArrayList<>();
    private final List<RegistroCambioEstado<EstadoDonacionIndependiente>> historialEstados = new ArrayList<>();

    public DonacionIndependiente(
        Bien bien
    ) {
        this.bien = bien;
        this.historialEstados.add(new RegistroCambioEstado<>(null, this.estado, new java.util.Date(), null));
    }

    public Bien getBien() {
        return this.bien;
    }

    public void usar(Integer cantidad) {
        if(cantidad < 0){
            throw new IllegalArgumentException("La cantidad a usar no puede ser negativa");
        }
        if(cantidad > this.getDisponible()){
            throw new RuntimeException();
        }
        this.usado += cantidad;
    }

    public void actualizarEstadoSegunUso(Necesidad necesidad) {
        if (this.getDisponible() == 0) {
            this.estado = EstadoDonacionIndependiente.ENTREGADA;
        } else {
            this.estado = EstadoDonacionIndependiente.EN_DEPOSITO;
        }
    }

    public Integer getDisponible() {
        return (Integer) (this.bien.getCantidad() - this.usado);
    }

    public EstadoDonacionIndependiente getEstado() {
        return this.estado;
    }

    public void setEstado(EstadoDonacionIndependiente estado) {
        if (this.estado != estado) {
            this.historialEstados.add(new RegistroCambioEstado<>(this.estado, estado, new java.util.Date(), null));
        }
        this.estado = estado;
    }

    public void agregarAsignacion(AsignacionItem<Necesidad, Integer> nuevaAsignacion) {
        this.asignaciones.add(nuevaAsignacion);
    }

    public List<AsignacionItem<Necesidad, Integer>> getAsignaciones() {
        return this.asignaciones;
    }

    public List<RegistroCambioEstado<EstadoDonacionIndependiente>> getHistorialEstados() {
        return historialEstados;
    }

    public boolean fueEntregadaDespuesDe(LocalDate fechaLimite) {
        return this.historialEstados.stream()
                .filter(registro -> registro.estadoNuevo() == EstadoDonacionIndependiente.ENTREGADA)
                .map(RegistroCambioEstado::fecha)
                .map(fechaDate -> fechaDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate())
                .anyMatch(fechaEntrega -> !fechaEntrega.isBefore(fechaLimite));
    }
}
