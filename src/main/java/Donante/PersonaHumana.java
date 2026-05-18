package Donante;

import java.util.List;

public class PersonaHumana extends Donante {
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
        List<TipoContacto> contactos,
        TipoContacto medioPredeterminado
    ) {
        super(tipoDoc, documento, nombre, contactos, medioPredeterminado);
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
}
