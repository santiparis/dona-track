import java.util.ArrayList;
import java.util.List;

public class DonacionIndependiente {
    private final Bien bien;
    private EstadoDonacion estado = EstadoDonacion.EN_DEPOSITO;
    private int usado = 0;
    private final List<AsignacionItem<Necesidad, Integer>> asignaciones = new ArrayList<>();

    public DonacionIndependiente(
        Bien bien
    ) {
        this.bien = bien;
    }

    public Bien getBien() {
        return this.bien;
    }

    public void usar(Integer cantidad) {
        if(cantidad > this.getDisponible()){
            throw new RuntimeException();
        }
        this.usado += cantidad;
    }

    public Integer getDisponible() {
        return (Integer) (this.bien.getCantidad() - this.usado);
    }

    public EstadoDonacion getEstado() {
        return this.estado;
    }

    public void setEstado(EstadoDonacion estado) {
        this.estado = estado;
    }

    public void agregarAsignacion(AsignacionItem<Necesidad, Integer> nuevaAsignacion) {
        this.asignaciones.add(nuevaAsignacion);
    }

    public List<AsignacionItem<Necesidad, Integer>> getAsignaciones() {
        return this.asignaciones;
    }
}
