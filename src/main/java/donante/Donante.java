package donante;

import java.util.ArrayList;
import java.util.List;

public abstract class Donante {
    private final TipoDoc tipoDoc;
    private String documento;
    private String nombre;
    private List<Contacto> contactos;
    private Contacto medioPredeterminado;
    private Usuario usuario;

    Donante(
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
        if (contactos.isEmpty()) {
            throw new IllegalArgumentException("La lista de contactos no puede ser nula o vacía.");
        }
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

  public String getEmail() {
    return contactos.get(0).getValor();
  }

  public abstract void actualizarseDesde(Donante donanteConNuevosDatos);

  protected void actualizarDatosComunes(Donante donante) {
        this.nombre = donante.getNombre();
        this.documento = donante.getDocumento();
        this.contactos = donante.getContactos();
        this.medioPredeterminado = donante.getMedioPredeterminado();
  }
}
