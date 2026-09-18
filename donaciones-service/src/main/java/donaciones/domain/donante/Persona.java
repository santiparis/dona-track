package donaciones.domain.donante;

import com.fasterxml.jackson.annotation.JsonIgnore;
import donaciones.domain.notificacion.Contacto;
import donaciones.domain.notificacion.Notificable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.*;

@Entity
@Table(name = "persona")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "tipo", discriminatorType = DiscriminatorType.STRING)
public abstract class Persona implements Notificable {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) @Column(name = "persona_id") private Long id;
    @Enumerated(EnumType.STRING) @Column(name = "tipo_doc") private TipoDoc tipoDoc;
    @Column(name = "documento") private String documento;
    @Column(name = "nombre") private String nombre;
    @Transient private List<Contacto> contactos = new ArrayList<>();
    @Transient private Contacto medioPredeterminado;
    @OneToOne(cascade = CascadeType.ALL) @JoinColumn(name = "usuario_id") private Usuario usuario;
    @Column(name = "ultima_interaccion") private LocalDateTime ultimaInteraccion = LocalDateTime.now();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    protected Persona() { }

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

    @JsonIgnore
    public Usuario getUsuario() {
        return usuario;
    }

    @JsonIgnore
    public String getEmail() {
        return this.contactos.isEmpty() ? null : this.contactos.get(0).getValor();
    }

  public abstract void actualizarseDesde(Persona donanteConNuevosDatos);

  protected void actualizarDatosComunes(Persona persona) {
        this.nombre = persona.getNombre();
        this.documento = persona.getDocumento();
        this.agregarContactos(persona.getContactos());
        this.setMedioPredeterminado(persona.getMedioPredeterminado());
  }

    public void actualizarDatosParciales(
            String nombre,
            String documento,
            String apellido,
            Integer edad,
            String direccion,
            String rubro,
            List<Contacto> contactos
    ) {
        if (nombre != null) {
            this.nombre = nombre;
        }
        if (documento != null) {
            this.documento = documento;
        }
        if (contactos != null) {
            this.agregarContactos(contactos);
        }
        this.actualizarDatosPropios(apellido, edad, direccion, rubro);
    }

    protected abstract void actualizarDatosPropios(String apellido, Integer edad, String direccion, String rubro);

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
