package donaciones.domain;

import donaciones.domain.notificacion.Contacto;
import donaciones.domain.notificacion.Notificable;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.*;
import org.hibernate.annotations.Where;

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
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "notificable_id", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    @Where(clause = "tipo_notificable = 'ENTIDAD'")
    @OrderBy("id")
    private List<Contacto> contactos = new ArrayList<>();
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
        this.contactos.forEach(contacto -> contacto.identificarNotificable("ENTIDAD"));
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
            this.contactos.forEach(contacto -> contacto.identificarNotificable("ENTIDAD"));
        }
        if (medioPredeterminado != null) {
            this.contactos.forEach(contacto -> contacto.marcarComoMedioPredeterminado(contacto == medioPredeterminado));
            this.medioPredeterminado = medioPredeterminado;
        }
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
        if (medioPredeterminado == null) {
            medioPredeterminado = contactoPredeterminado();
        }
        return this.medioPredeterminado;
    }

    public void registrarContacto(Contacto contacto, boolean predeterminado) {
        if (!this.contactos.contains(contacto)) {
            contacto.identificarNotificable("ENTIDAD");
            this.contactos.add(contacto);
        }
        if (predeterminado || this.medioPredeterminado == null) {
            this.medioPredeterminado = contacto;
            this.contactos.forEach(actual -> actual.marcarComoMedioPredeterminado(actual == contacto));
        }
    }

    private Contacto contactoPredeterminado() {
        return this.contactos.stream()
            .filter(Contacto::esMedioPredeterminado)
            .findFirst()
            .orElse(this.contactos.isEmpty() ? null : this.contactos.get(0));
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
