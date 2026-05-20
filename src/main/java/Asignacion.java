import java.util.ArrayList;
import java.util.List;

public class Asignacion {
    private final Necesidad necesidad;
    private final List<Bien> bienes;
    private final EstadoDonacion estado;

    public Asignacion(Necesidad necesidad, EstadoDonacion estado) {
        this.necesidad = necesidad;
        this.bienes = new ArrayList<>();
        this.estado = estado;
    }

    public void agregarBien(Bien nuevoBien) {
        this.bienes.add(nuevoBien);
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
}
