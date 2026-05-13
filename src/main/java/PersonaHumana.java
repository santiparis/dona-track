public class PersonaHumana extends Donante {
    private final TipoContacto medioPredeterminado;

    public PersonaHumana(
            TipoDoc tipoDoc,
            String documento,
            String nombre_razonSocial,
            String email,
            TipoContacto medioPredeterminado
    ) {
        super(tipoDoc, documento, nombre_razonSocial, email);
        this.medioPredeterminado = medioPredeterminado;
    }

    public TipoContacto getMedioPredeterminado() {
        return this.medioPredeterminado;
    }
}
