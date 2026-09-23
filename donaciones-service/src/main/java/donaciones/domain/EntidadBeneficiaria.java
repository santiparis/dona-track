package donaciones.domain;

import donaciones.domain.notificacion.Contacto;
import donaciones.domain.notificacion.Notificable;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.*;

@Entity
@Table(name = "entidad_beneficiaria")
public class EntidadBeneficiaria implements Notificable {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) @Column(name = "entidad_id") private Long id;
    @Column(name = "razon_social") private String razonSocial;
    @Column(name = "direccion") private String direccion;
    @Column(name = "telefono") private String telefono;
    @ElementCollection @CollectionTable(name = "correo_representante", joinColumns = @JoinColumn(name = "entidad_id"))
    @Column(name = "correo") private List<String> correosRepresentantes = new ArrayList<>();
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true) @JoinColumn(name = "entidad_id")
    private List<Necesidad> necesidades = new ArrayList<>();
    @Transient private List<Contacto> contactos = new ArrayList<>();
    @Transient private Contacto medioPredeterminado;
    @OneToMany(mappedBy = "entidadBeneficiaria") private List<Donacion> donacionesRecibidas = new ArrayList<>();

    protected EntidadBeneficiaria() { }

    public EntidadBeneficiaria(
            String razonSocial,
            String direccion,
            String telefono,
            List<String> correosRepresentantes
    ) {
        this.razonSocial = razonSocial;
        this.direccion = direccion;
        this.telefono = telefono;
        this.correosRepresentantes = correosRepresentantes;
    }

    public EntidadBeneficiaria(
            String razonSocial,
            String direccion,
            String telefono,
            List<String> correosRepresentantes,
            List<Contacto> contactos,
            Contacto medioPredeterminado
    ) {
        this(razonSocial, direccion, telefono, correosRepresentantes);
        if (contactos != null) {
            this.contactos.addAll(contactos);
        }
        this.medioPredeterminado = medioPredeterminado;
    }

    public String getRazonSocial() {
        return this.razonSocial;
    }

    public String getDireccion() {
        return this.direccion;
    }

    public String getTelefono() {
        return this.telefono;
    }

    public List<String> getCorreosRepresentantes() {
        return this.correosRepresentantes;
    }

    public void actualizarDatos(String razonSocial, String direccion, String telefono, List<String> correosRepresentantes) {
        if (razonSocial != null) {
            this.razonSocial = razonSocial;
        }
        if (direccion != null) {
            this.direccion = direccion;
        }
        if (telefono != null) {
            this.telefono = telefono;
        }
        if (correosRepresentantes != null) {
            this.correosRepresentantes = correosRepresentantes;
        }
    }

    public void registrarNecesidad(Necesidad necesidad) {
        if (!this.necesidades.contains(necesidad)) {
            this.necesidades.add(necesidad);
        }
    }

    public List<Necesidad> getNecesidades() {
        return necesidades;
    }

    @Override
    public List<Contacto> getContactos() {
        return this.contactos;
    }

    @Override
    public Contacto getMedioPredeterminado() {
        return this.medioPredeterminado;
    }

    public void registrarContacto(Contacto contacto, boolean predeterminado) {
        if (!this.contactos.contains(contacto)) {
            this.contactos.add(contacto);
        }
        if (predeterminado || this.medioPredeterminado == null) {
            this.medioPredeterminado = contacto;
        }
    }

    public boolean satisfaceNecesidad(Donacion donacion) {
        if (donacion.getBien() == null) {
            return false;
        }
        return this.necesidades.stream()
                .anyMatch(necesidad -> necesidad.seSatisfaceCon(donacion.getBien()));
    }

    public int getDonacionesUltimoTrimestre() {
        LocalDate haceTresMeses = LocalDate.now().minusMonths(3);
        long cantidad = this.donacionesRecibidas.stream()
                .filter(donacion -> donacion.getFecha().isAfter(haceTresMeses))
                .count();
        return (int) cantidad;
    }

    public void eliminarNecesidadPorId(Long idNecesidad) {
        this.necesidades.removeIf(n -> n.getId() != null && n.getId().equals(idNecesidad));
    }

    public void actualizarNecesidadPorId(Long idNecesidad, Necesidad necesidad) {
        for (int i = 0; i < this.necesidades.size(); i++) {
            Necesidad actual = this.necesidades.get(i);
            if (actual.getId() != null && actual.getId().equals(idNecesidad)) {
                necesidad.setId(idNecesidad);
                this.necesidades.set(i, necesidad);
                return;
            }
        }
    }
  
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
