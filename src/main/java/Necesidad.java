import java.util.HashMap;
import java.util.List;

public abstract class Necesidad {
    private final EntidadBeneficiaria entidad;
    private final String descripcion;
    private final List<Bien> bienes;

    public Necesidad(
            EntidadBeneficiaria entidad,
            String descripcion,
            List<Bien> bienes
    ) {
        this.entidad = entidad;
        this.descripcion = descripcion;
        this.bienes = bienes;
    }

    public EntidadBeneficiaria getEntidad() {
        return this.entidad;
    }

    public String getDescripcion() {
        return this.descripcion;
    }

    public List<Bien> getBienes() {
        return this.bienes;
    }

    public HashMap<Subcategoria, Integer> getCantidades() {
        HashMap<Subcategoria, Integer> cantidades = new HashMap<>();
        for(Bien bien : this.bienes) {
            cantidades.put(bien.getSubcategoria(), bien.getCantidad());
        }
        return cantidades;
    }

    public void actualizar() {}

    public void resolver() {}
}
