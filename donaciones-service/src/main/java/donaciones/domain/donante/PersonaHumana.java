package donaciones.domain.donante;

import java.util.List;

public class PersonaHumana extends Persona {
    private String apellido;
    private Integer edad;
    private Genero genero;
    private String direccion;

    public PersonaHumana(
        String nombre,
        String apellido,
        Integer edad,
        TipoDoc tipoDoc,
        String documento,
        Genero genero,
        String direccion,
        List<Contacto> contactos,
        Contacto medioPredeterminado,
        Usuario usuario
    ) {
        super(tipoDoc, documento, nombre, contactos, medioPredeterminado, usuario);
        this.apellido = apellido;
        this.edad = edad;
        this.genero = genero;
        this.direccion = direccion;
    }

    public String getApellido() {
        return apellido;
    }

    public Integer getEdad() {
        return edad;
    }

    public Genero getGenero() {
        return genero;
    }

    public String getDireccion() {
        return direccion;
    }

    @Override
    public void actualizarseDesde(Persona personaConNuevosDatos) {
        if (personaConNuevosDatos instanceof PersonaHumana nuevosDatos) {
          super.actualizarDatosComunes(nuevosDatos);
            this.apellido = nuevosDatos.getApellido();
            this.edad = nuevosDatos.getEdad();
            this.genero = nuevosDatos.getGenero();
            this.direccion = nuevosDatos.getDireccion();
        } else {
            throw new IllegalArgumentException("Incompatibilidad de tipos: no se puede actualizar una PersonaHumana con datos de PersonaJuridica");
        }
    }
}
