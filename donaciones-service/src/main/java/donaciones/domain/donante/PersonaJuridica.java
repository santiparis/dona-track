package donaciones.domain.donante;

import donaciones.domain.notificacion.Contacto;
import java.util.List;
import javax.persistence.*;

@Entity
@DiscriminatorValue("JURIDICA")
public class PersonaJuridica extends Persona {
    @Enumerated(EnumType.STRING) @Column(name = "razon_social") private RazonSocial razonSocial;
    @Column(name = "rubro") private String rubro;
    @ManyToMany private List<PersonaHumana> representantesHabilitados;

    protected PersonaJuridica() { }

    public PersonaJuridica(
            TipoDoc tipoDoc,
            String documento,
            String nombre,
            RazonSocial razonSocial,
            String rubro,
            List<PersonaHumana> representantesHabilitados,
            List<Contacto> contactos,
            Contacto medioPredeterminado,
            Usuario usuario
    ) {
        super(tipoDoc, documento, nombre, contactos, medioPredeterminado, usuario);
        this.razonSocial = razonSocial;
        this.rubro = rubro;
        this.representantesHabilitados = representantesHabilitados;
    }

    public RazonSocial getRazonSocial() {
        return razonSocial;
    }

    public String getRubro() {
        return rubro;
    }

    public List<PersonaHumana> getRepresentantesHabilitados() {
        return representantesHabilitados;
    }

    @Override
    public void actualizarseDesde(Persona personaConNuevosDatos) {
        if (personaConNuevosDatos instanceof PersonaJuridica nuevosDatos) {
            super.actualizarDatosComunes(nuevosDatos);
            this.rubro = nuevosDatos.getRubro();
            this.representantesHabilitados = nuevosDatos.getRepresentantesHabilitados();
        } else {
            throw new IllegalArgumentException("Incompatibilidad de tipos: no se puede actualizar una PersonaJuridica con datos de " + personaConNuevosDatos.getClass().getSimpleName());
        }
    }

    @Override
    protected void actualizarDatosPropios(String apellido, Integer edad, String direccion, String rubro) {
        if (rubro != null) {
            this.rubro = rubro;
        }
    }
}
