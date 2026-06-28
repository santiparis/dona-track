import java.util.ArrayList;
import java.util.List;
import java.time.LocalDate;

public class EntidadBeneficiaria {
    private final String razonSocial;
    private final String direccion;
    private final String telefono;
    private final List<String> correosRepresentantes;
    private final List<Necesidad> necesidades = new ArrayList<>();

    public EntidadBeneficiaria(
            String razonSocial,
            String direccion,
            String telefono,
            List<String> correosRepresentantes
    ) {
        this.razonSocial = razonSocial;
        this.direccion = direccion;
        this.telefono = telefono;
        this.correosRepresentantes = correosRepresentantes;
    }

    public String getRazonSocial() {
        return this.razonSocial;
    }

    public String getDireccion() {
        return this.direccion;
    }

    public String getTelefono() {
        return this.telefono;
    }

    public List<String> getCorreosRepresentantes() {
        return this.correosRepresentantes;
    }

    public void registrarNecesidad(Necesidad necesidad) {
        if (!this.necesidades.contains(necesidad)) {
            this.necesidades.add(necesidad);
        }
    }

    public List<Necesidad> getNecesidades() {
        return necesidades;
    }

    public boolean necesitaSubcategoria(Subcategoria subcategoriaDonada) {
        return this.necesidades.stream().anyMatch(necesidad -> necesidad.getSubcategoria().equals(subcategoriaDonada));
    }

    public long getDonacionesRecibidasUltimoTrimestre() {
        LocalDate haceTresMeses = LocalDate.now().minusMonths(3);

        return this.donacionesRecibidas.stream().filter(donacion -> donacion.getFechaEntrega() != null).filter(donacion -> donacion.getFechaEntrega().isAfter(haceTresMeses)).count();
    }

}
