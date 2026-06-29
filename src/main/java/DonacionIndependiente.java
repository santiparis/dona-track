import java.util.ArrayList;
import java.util.List;

public class DonacionIndependiente {
    private final Bien bien;
    private EstadoDonacionIndependiente estado = EstadoDonacionIndependiente.EN_DEPOSITO;
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

    public EstadoDonacionIndependiente getEstado() {
        return this.estado;
    }

    public void setEstado(EstadoDonacionIndependiente estado) {
        if (this.estado != estado) {
            this.historialEstados.add(new RegistroCambioEstado<>(this.estado, estado, new java.util.Date(), null));
        }
        this.estado = estado;
    }

    public List<RegistroCambioEstado<EstadoDonacionIndependiente>> getHistorialEstados() {
        return historialEstados;
    }
}
