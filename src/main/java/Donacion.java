import java.util.List;

public class Donacion {
    private final Donante donante;
    private final String descripcion;
    private final List<Bien> bienes;

    public Donacion(
            Donante donante,
            String descripcion,
            List<Bien> bienes
    ) {
        this.donante = donante;
        this.descripcion = descripcion;
        this.bienes = bienes;
    }

    public Donante getDonante() {
        return this.donante;
    }

    public String getDescripcion() {
        return this.descripcion;
    }

    public List<Bien> getBienes() {
        return this.bienes;
    }
}
