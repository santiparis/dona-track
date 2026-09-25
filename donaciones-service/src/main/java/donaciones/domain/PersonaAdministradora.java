package donaciones.domain;

import donaciones.domain.notificacion.Contacto;
import donaciones.domain.notificacion.Notificable;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.*;


@Entity
@Table(name = "persona_administradora")
public class PersonaAdministradora implements Notificable {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) @Column(name = "persona_administradora_id") private Long id;
    private String nombre;
    @Transient private List<Contacto> contactos = new ArrayList<>();
    @Transient private Contacto medioPredeterminado;

    protected PersonaAdministradora() { }

    public PersonaAdministradora(String nombre, List<Contacto> contactos, Contacto medioPredeterminado) {
        this.nombre = nombre;
        if (contactos != null) {
            this.contactos.addAll(contactos);
        }
        this.medioPredeterminado = medioPredeterminado;
    }

    public Long getId() { return id; }

    public String getNombre() {
        return nombre;
    }

    @Override
    public List<Contacto> getContactos() {
        return contactos;
    }

    public void reconstruirContactos(List<Contacto> contactos) {
        this.contactos = new ArrayList<>(contactos);
        this.medioPredeterminado = this.contactos.isEmpty() ? null : this.contactos.get(0);
    }

    @Override
    public Contacto getMedioPredeterminado() {
        return medioPredeterminado;
    }

    // TODO: Implementar confirmación de destino final de donaciones tras la ejecución de los algoritmos de matchmaking.
    // TODO: Implementar revisión y gestión de entregas no satisfactorias (para decidir replanificación o regreso a depósito).
    // TODO: Implementar acceso y visualización del dashboard de monitoreo de camiones en tiempo real.
}
