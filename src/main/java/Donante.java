public class Donante {
    private final TipoDoc tipoDoc;
    private final String documento;
    private final String nombre_razonSocial;
    private final String email;

    public Donante(
            TipoDoc tipoDoc,
            String documento,
            String nombre_razonSocial,
            String email
            ) {
        this.tipoDoc = tipoDoc;
        this.documento = documento;
        this.nombre_razonSocial = nombre_razonSocial;
        this.email = email;
    }

    public Donante(
            TipoDoc tipoDoc,
            String documento,
            String nombre_razonSocial,
            String email,
            String telefono,
            Boolean tieneWpp
    ) {
        if(tieneWpp && telefono == null){

        }
        this.tipoDoc = tipoDoc;
        this.documento = documento;
        this.nombre_razonSocial = nombre_razonSocial;
        this.email = email;
    }

    public TipoDoc getTipoDoc() {
        return this.tipoDoc;
    }

    public String getDocumento() {
        return this.documento;
    }

    public String getNombre_razonSocial() {
        return this.nombre_razonSocial;
    }

    public String getEmail() {
        return this.email;
    }
}
