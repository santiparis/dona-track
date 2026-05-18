package Donante;

import java.util.ArrayList;
import java.util.List;

public class Donante {
    private final TipoDoc tipoDoc;
    private final String documento;
    private String nombre;
    private List<TipoContacto> contactos;
    private TipoContacto medioPredeterminado;

    public Donante(
            TipoDoc tipoDoc,
            String documento,
            String nombre,
            List<TipoContacto> contactos,
            TipoContacto medioPredeterminado
            ) {
        this.tipoDoc = tipoDoc;
        this.documento = documento;
        this.nombre = nombre;
        this.contactos = new ArrayList<>(contactos);
        //TODO: Validar lista de contactos no vacia
        this.medioPredeterminado = medioPredeterminado;
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

    public List<TipoContacto> getContactos() {
        return contactos;
    }

    public TipoContacto getMedioPredeterminado() {
        return medioPredeterminado;
    }
}
