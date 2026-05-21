import java.util.ArrayList;
import java.util.List;

public class Asignacion {
    private final Necesidad necesidad;
    private final EstadoDonacion estado;

    public Asignacion(Necesidad necesidad, EstadoDonacion estado) {
        this.necesidad = necesidad;
        this.estado = estado;
    }


    public List<Bien> getBienes() {
        return necesidad.getBienes();
    }

    public Necesidad getNecesidad() {
        return necesidad;
    }

    public EstadoDonacion getEstado() {
        return estado;
    }
}
