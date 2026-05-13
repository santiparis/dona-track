import java.util.List;

public class EntidadBeneficiaria {
    private final String razonSocial;
    private final String direccion;
    private final String telefono;
    private final List<String> correosRepresentantes;

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
}
