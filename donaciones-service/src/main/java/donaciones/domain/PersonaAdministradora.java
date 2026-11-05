package donaciones.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import donaciones.domain.donante.Usuario;
import donaciones.domain.notificacion.Contacto;
import donaciones.domain.notificacion.Notificable;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.*;
import org.hibernate.annotations.Where;


@Entity
@Table(name = "persona_administradora")
public class PersonaAdministradora implements Notificable {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) @Column(name = "persona_administradora_id") private Long id;
    private String nombre;
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "usuario_id", unique = true)
    private Usuario usuario;
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "notificable_id", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    @Where(clause = "tipo_notificable = 'ADMINISTRADORA'")
    @OrderBy("id")
    private List<Contacto> contactos = new ArrayList<>();
    @Transient private Contacto medioPredeterminado;

    protected PersonaAdministradora() { }

    public PersonaAdministradora(String nombre, List<Contacto> contactos, Contacto medioPredeterminado) {
        this(nombre, contactos, medioPredeterminado, null);
    }

    public PersonaAdministradora(String nombre, List<Contacto> contactos, Contacto medioPredeterminado,
                                 Usuario usuario) {
        this.nombre = nombre;
        this.usuario = usuario;
        if (contactos != null) {
            this.contactos.addAll(contactos);
            this.contactos.forEach(contacto -> contacto.identificarNotificable("ADMINISTRADORA"));
        }
        if (medioPredeterminado != null) {
            this.contactos.forEach(contacto -> contacto.marcarComoMedioPredeterminado(contacto == medioPredeterminado));
            this.medioPredeterminado = medioPredeterminado;
        }
    }

    public Long getId() { return id; }

    @JsonIgnore
    public Usuario getUsuario() {
        return usuario;
    }

    public String getNombre() {
        return nombre;
    }

    @Override
    public List<Contacto> getContactos() {
        return contactos;
    }

    @Override
    public Contacto getMedioPredeterminado() {
        if (medioPredeterminado == null) {
            medioPredeterminado = contactoPredeterminado();
        }
        return medioPredeterminado;
    }

    private Contacto contactoPredeterminado() {
        return this.contactos.stream()
            .filter(Contacto::esMedioPredeterminado)
            .findFirst()
            .orElse(this.contactos.isEmpty() ? null : this.contactos.get(0));
    }

    // TODO: Implementar confirmación de destino final de donaciones tras la ejecución de los algoritmos de matchmaking.
    // TODO: Implementar revisión y gestión de entregas no satisfactorias (para decidir replanificación o regreso a depósito).
    // TODO: Implementar acceso y visualización del dashboard de monitoreo de camiones en tiempo real.
}
