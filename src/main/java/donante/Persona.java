package donante;

import notificacion.Notificable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public abstract class Persona implements Notificable {
    private final TipoDoc tipoDoc;
    private String documento;
    private String nombre;
    private final List<Contacto> contactos;
    private Contacto medioPredeterminado;
    private final Usuario usuario;
    private LocalDateTime ultimaInteraccion = LocalDateTime.now();

    Persona(
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
        setMedioPredeterminado(medioPredeterminado);
        this.usuario = usuario;
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

    public void agregarContactos(List<Contacto> nuevosContactos) {
        nuevosContactos.forEach(nuevoContacto -> {
            if (!this.contactos.contains(nuevoContacto)) {
                this.contactos.add(nuevoContacto);
            }
        });
    }

    public Contacto getMedioPredeterminado() {
        return medioPredeterminado;
    }

    public void setMedioPredeterminado(Contacto medioPredeterminado) {
        if (!this.contactos.contains(medioPredeterminado)) {
            throw new IllegalArgumentException("El medio predeterminado debe existir en la lista de contactos.");
        }
        this.medioPredeterminado = medioPredeterminado;
    }

    public Usuario getUsuario() {
        return usuario;
    }

  public String getEmail() {
    return contactos.get(0).getValor();
  }

  public abstract void actualizarseDesde(Persona donanteConNuevosDatos);

  protected void actualizarDatosComunes(Persona persona) {
        this.nombre = persona.getNombre();
        this.documento = persona.getDocumento();
        this.agregarContactos(persona.getContactos());
        this.setMedioPredeterminado(persona.getMedioPredeterminado());
  }

    public LocalDateTime getUltimaInteraccion() {
        return ultimaInteraccion;
    }

    public void registrarInteraccion() {
        this.ultimaInteraccion = LocalDateTime.now();
    }

    public void setUltimaInteraccion(LocalDateTime ultimaInteraccion) {
        this.ultimaInteraccion = ultimaInteraccion;
    }
}
