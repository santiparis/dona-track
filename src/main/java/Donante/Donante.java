package Donante;

import java.util.ArrayList;
import java.util.List;

public abstract class Donante {
    private final TipoDoc tipoDoc;
    private final String documento;
    private String nombre;
    private List<Contacto> contactos;
    private Contacto medioPredeterminado;
    private Usuario usuario;

    public Donante(
            TipoDoc tipoDoc,
            String documento,
            String nombre,
            List<Contacto> contactos,
            Contacto medioPredeterminado,
            Usuario usuario
            ) {
        this.tipoDoc = tipoDoc;
        this.documento = documento;
        this.nombre = nombre;
        this.contactos = new ArrayList<>(contactos);
        //TODO: Validar lista de contactos no vacia
        this.medioPredeterminado = medioPredeterminado;
        this.usuario = usuario;
    }

    public TipoDoc getTipoDoc() {
        return this.tipoDoc;
    }

    public String getDocumento() {
        return this.documento;
    }

    public String getNombre() {
        return this.nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public List<Contacto> getContactos() {
        return contactos;
    }

    public Contacto getMedioPredeterminado() {
        return medioPredeterminado;
    }

    public Usuario getUsuario() {
        return usuario;
    }
}
